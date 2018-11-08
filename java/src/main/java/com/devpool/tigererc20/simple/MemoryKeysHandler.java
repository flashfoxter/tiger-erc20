package com.devpool.tigererc20.simple;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.web3j.utils.Numeric;

import com.devpool.tigererc20.KeysHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Sample implementation of {@link KeysHandler}.
 */
@Lazy
@Slf4j
@Component
public class MemoryKeysHandler implements KeysHandler {

    private Map<String, BigInteger> storedKeys = new ConcurrentHashMap<>();


    @Override
    public void store(BigInteger privateKey, BigInteger publicKey, String address) {
        log.info("Key chain is stored. Private key:{}, public key: {}, address: {}", privateKey, publicKey, address);
        storedKeys.put(address, privateKey);
    }


    @Override
    public String getPrivateKeyForAddress(String address) {
        return Numeric.toHexStringWithPrefix(storedKeys.get(address));
    }


    @Override
    public boolean isAddressExists(String address) {
        return storedKeys.containsKey(address);
    }


    @Override
    public Collection<String> getAllAddresses() {
        return storedKeys.keySet();
    }

}
