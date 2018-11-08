package com.devpool.tigererc20;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;

@Lazy
@Component
public class HttpWeb3jServiceFactory {

    @Autowired
    private EthConfiguration config;


    /**
     * Gets instance of {@link Web3jService}.
     * 
     * @return {@link Web3jService}.
     */
    public Web3jService getWeb3jService() {
        return new HttpService(config.getWeb3jServiceUrl());
    }

}
