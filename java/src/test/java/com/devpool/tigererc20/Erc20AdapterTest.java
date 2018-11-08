package com.devpool.tigererc20;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class Erc20AdapterTest {

    @Mock
    private Erc20Provider erc20Provider;

    @Mock
    private KeysHandler keysHandler;

    @Mock
    private Erc20 token;

    @Mock
    private EthConfiguration config;

    @Mock
    private Web3j web3j;

    @InjectMocks
    private EthAdapter adapter;


    @Test
    public void generateWalletTest() throws Exception {
        String address = adapter.generateWallet();
        verify(keysHandler).store(argThat(pr -> pr != null), argThat(pb -> pb != null),
                argThat(addr -> StringUtils.equals(address, addr)));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void getBalanceTest() throws Exception {
        String address = "0xfec39a93eec6bca07e14d196cc0c135df596ff82";
        BigInteger intBalance = BigInteger.valueOf(3L);
        BigInteger decimals = BigInteger.valueOf(18L);
        RemoteCall<BigInteger> getBallanceCall = mock(RemoteCall.class);
        RemoteCall<BigInteger> decimalsCall = mock(RemoteCall.class);
        doReturn(getBallanceCall).when(token).balanceOf(address);
        doReturn(decimalsCall).when(token).decimals();
        doReturn(intBalance).when(getBallanceCall).send();
        doReturn(decimals).when(decimalsCall).send();

        BigDecimal balance = adapter.getBalance(address);
        assertEquals(BigDecimal.valueOf(intBalance.longValue(), decimals.intValue()), balance);
    }
}
