package com.devpool.tigererc20;

import java.math.BigInteger;

import org.web3j.tx.gas.ContractGasProvider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Gas provider implementation.
 */
@RequiredArgsConstructor
public class GasProvider implements ContractGasProvider {

    @NonNull
    private BigInteger gasPrice;

    @NonNull
    private BigInteger gasLimit;


    @Override
    public BigInteger getGasPrice(String contractFunc) {
        return gasPrice;
    }


    @Override
    public BigInteger getGasPrice() {
        throw new UnsupportedOperationException();
    }


    @Override
    public BigInteger getGasLimit(String contractFunc) {
        return gasLimit;
    }


    @Override
    public BigInteger getGasLimit() {
        throw new UnsupportedOperationException();
    }

}
