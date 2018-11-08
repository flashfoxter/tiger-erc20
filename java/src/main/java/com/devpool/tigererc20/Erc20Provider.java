package com.devpool.tigererc20;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import lombok.Getter;

/**
 * Factory class for loading {@link Erc20} token.
 * <br/>It's helpful for testing to have it.
 */
@Component
public class Erc20Provider {

    @Autowired
    private HttpWeb3jServiceFactory web3jServiceFactory;

    @Autowired
    private EthConfiguration config;

    @Getter
    private Web3j web3j;


    @PostConstruct
    public void init() {
        web3j = Web3j.build(web3jServiceFactory.getWeb3jService());
    }


    /**
     * Gets instance of Erc20 token.
     * 
     * @param credentials credentials.
     * @param gasProvider gas provider
     * @return {@link Erc20}.
     */
    public Erc20 getToken(Credentials credentials, GasProvider gasProvider) {
        return Erc20.load(config.getTokenAddress(), web3j, credentials, gasProvider);
    }

}
