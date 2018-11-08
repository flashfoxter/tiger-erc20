package com.devpool.tigererc20;

import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.Credentials;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class IntegrationTest {

    @Autowired
    private EthAdapter ethAdapter;

    @Autowired
    private Erc20Provider erc20Provider;

    @Autowired
    private EthConfiguration config;


    @Test
    @Ignore
    public void erc20Test() throws Exception {

        ethAdapter.subscribeOnTransfers((a, b) -> {
            log.info("Deposite for {} is received. current coin balance: {}", a, b);
        });

        String newAddr = ethAdapter.generateWallet();

        Erc20 token = erc20Provider.getToken(Credentials.create(config.getMasterPrivateKey()),
                new GasProvider(config.getGasPrice(), config.getGasLimit()));

        token.transfer(newAddr, BigInteger.TEN).send();

        Thread.sleep(120_000);

        ethAdapter.withdraw("0x4926adA10df9B064D8Ed441eBBFEC50EA2d6FdD1", new BigDecimal(1e-17));

        Assert.assertEquals(BigDecimal.ZERO.setScale(18), ethAdapter.getBalance(newAddr));

    }


    @Test
    public void totalSupplyNotNullTtest() throws Exception {
        Erc20 token = erc20Provider.getToken(Credentials.create(config.getMasterPrivateKey()),
                new GasProvider(config.getGasPrice(), config.getGasLimit()));

        BigInteger totalSupply = token.totalSupply().send();
        log.info("Total supply of True USD: {}", totalSupply);
        assertNotNull(totalSupply);
    }

}
