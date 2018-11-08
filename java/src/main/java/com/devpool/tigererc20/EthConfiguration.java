package com.devpool.tigererc20;

import java.math.BigInteger;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration class for ETH integration.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eth")
public class EthConfiguration {

    /** Url of http web3j service (for example infura). */
    private String web3jServiceUrl;

    /** Token contract address. */
    private String tokenAddress;

    /** Private key of the master account. */
    private String masterPrivateKey;

    /** Address of the master account. */
    private String masterAddress;

    private BigInteger gasLimit = BigInteger.valueOf(100_000);

    private BigInteger gasPrice = BigInteger.valueOf(21_000_000_000L);

    /** If we use timer instead of actual event subscriber (e.g. in the case of infura using) */
    private boolean emulateEvents;

    /** if emulateEvents == true than set this delay in ms. */
    private long emulateEventsDelay = 60_000;

}
