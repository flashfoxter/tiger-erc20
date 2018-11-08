package com.devpool.tigererc20;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert.Unit;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;

import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for integration with Ethereum network.
 */
@Component
@Slf4j
public class EthAdapter {

    @Autowired
    private Erc20Provider erc20Provider;

    @Autowired
    private KeysHandler keysHandler;

    @Autowired
    private EthConfiguration config;

    private ScheduledExecutorService eventEmulatorService = null;

    private ExecutorService transferService = Executors.newCachedThreadPool();

    private Map<String, Boolean> pendingAddresses = new ConcurrentHashMap<>();

    private Erc20 token;


    @PostConstruct
    public void init() {
        token = erc20Provider.getToken(Credentials.create(config.getMasterPrivateKey()),
                new GasProvider(config.getGasPrice(), config.getGasLimit()));
    }


    @PreDestroy
    public void destroy() {
        if (eventEmulatorService != null) {
            eventEmulatorService.shutdown();
        }
        transferService.shutdown();
    }


    /**
     * Creates a wallet using SECP-256k1 curve.
     * 
     * @return address of created wallet.
     */
    public String generateWallet()
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair keyPair = Keys.createEcKeyPair();
        BigInteger privateKey = keyPair.getPrivateKey();
        BigInteger publicKey = keyPair.getPublicKey();
        Credentials credentials = Credentials.create(new ECKeyPair(privateKey, publicKey));
        String address = credentials.getAddress();
        keysHandler.store(privateKey, publicKey, address);
        return address;
    }


    /**
     * Gets balance of token.
     * 
     * @param address address of wallet.
     * @return scaled value of balance.
     */
    public BigDecimal getBalance(String address) throws Exception {
        BigInteger intBalance = token.balanceOf(address).send();
        BigInteger decimals = token.decimals().send();
        return BigDecimal.valueOf(intBalance.longValue(), decimals.intValue());
    }


    /**
     * Subscribes to transfer on the token event.
     * 
     * @param transferConsumer consumer with address of transfer participant as 1st parameter and new value as second.
     */
    public void subscribeOnTransfers(BiConsumer<String, BigDecimal> transferConsumer) {
        if (config.isEmulateEvents()) {
            eventEmulatorService = Executors.newSingleThreadScheduledExecutor();
            eventEmulatorService.scheduleWithFixedDelay(() -> {
                keysHandler.getAllAddresses().forEach(address -> {
                    if (pendingAddresses.containsKey(address)) {
                        return;
                    }
                    try {
                        BigInteger intBalance = token.balanceOf(address).send();
                        if (intBalance.compareTo(BigInteger.ZERO) > 0) {
                            log.info("Transfer to {} received for {}", address, intBalance);
                            pendingAddresses.put(address, true);
                            // transfer coins to master account
                            transferService.execute(() -> {
                                try {
                                    transfer(address, intBalance, transferConsumer);
                                    pendingAddresses.remove(address);
                                } catch (Exception e) {
                                    log.error("Eror when event received", e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.error("Eror when check balance", e);
                    }
                });
            }, 0L, config.getEmulateEventsDelay(), TimeUnit.MILLISECONDS);
        } else {
            token.transferEventObservable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                    .subscribe(response -> {
                        if (keysHandler.isAddressExists(response.to)) {
                            log.info("Transfer {} from {} to {} received", response.value, response.from, response.to);
                            // deposit received

                            // transfer coins to master account
                            transferService.execute(() -> {
                                try {
                                    transfer(response.to, response.value, transferConsumer);
                                } catch (Exception e) {
                                    log.error("Eror when event received", e);
                                }
                            });

                        }
                    });
        }
    }


    /**
     * Withdraw coins.
     * 
     * @param address address to withdraw.
     * @param value value to withdraw.
     */
    public void withdraw(String address, BigDecimal value) throws Exception {
        BigInteger decimals = token.decimals().send();
        BigInteger intValue = value.multiply(BigDecimal.TEN.pow(decimals.intValue())).toBigInteger();
        TransactionReceipt receipt = token.transfer(address, intValue).send();
        log.info("withdraw receipt: {}", receipt);
    }


    /**
     * Transfers coins from {@code from} account to our main account.
     */
    @SuppressWarnings("rawtypes")
    private void transfer(String from, BigInteger value, BiConsumer<String, BigDecimal> transferConsumer)
            throws Exception {
        BigDecimal transferedBalance = getBalance(from);

        Web3j web3j = erc20Provider.getWeb3j();

        // calculate how many gas account from have to have to transfer coins
        BigInteger nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send()
                .getTransactionCount();
        final Function function = new Function(Erc20.FUNC_TRANSFER,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(config.getMasterAddress()),
                        new org.web3j.abi.datatypes.generated.Uint256(value)),
                Collections.<TypeReference<?>>emptyList());
        Transaction tr = Transaction.createFunctionCallTransaction(from, nonce, config.getGasPrice(),
                config.getGasLimit(), config.getTokenAddress(), FunctionEncoder.encode(function));
        BigInteger gasUsed = web3j.ethEstimateGas(tr).send().getAmountUsed();

        log.info("Estimated gas for transfer {} from {}: {}", value, from, gasUsed);

        // transfer ether to account "from" for gas needs
        Transfer.sendFunds(web3j, Credentials.create(config.getMasterPrivateKey()), from,
                new BigDecimal(gasUsed.multiply(config.getGasPrice())), Unit.WEI).send();

        // transfer tokens
        Erc20 transferToken = erc20Provider.getToken(Credentials.create(keysHandler.getPrivateKeyForAddress(from)),
                new GasProvider(config.getGasPrice(), gasUsed));
        TransactionReceipt receipt = transferToken.transfer(config.getMasterAddress(), value).send();
        log.info("Transfer receipt: {}", receipt);

        if (transferConsumer != null) {
            transferConsumer.accept(from, transferedBalance);
        }
    }

}
