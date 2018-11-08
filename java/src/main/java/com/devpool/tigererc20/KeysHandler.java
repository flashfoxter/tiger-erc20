package com.devpool.tigererc20;

import java.math.BigInteger;
import java.util.Collection;

/**
 * Methods to handle crypto keys.
 */
public interface KeysHandler {

    /**
     * Stores key info to persistence.
     * 
     * @param privateKey private key.
     * @param publicKey public key.
     * @param address ethereum address.
     */
    void store(BigInteger privateKey, BigInteger publicKey, String address);


    /**
     * Gets private key for address specified.
     * 
     * @param address ehtereum address.
     * @return private key.
     */
    String getPrivateKeyForAddress(String address);


    /**
     * Checks if passed address exists in the system.
     * 
     * @param address address to check.
     * @return true if address exists in the system or false other vise.
     */
    boolean isAddressExists(String address);


    /**
     * Gets all existing in the system addresses.
     * 
     * @return collection of addresses
     */
    Collection<String> getAllAddresses();
}
