package org.sonatype.nexus.restlight.common;


import org.junit.Test;

/**
 * Tests for {@link ProxyConfig}
 * @author plynch
 */
public class ProxyConfigTest {


    @Test(expected=NullPointerException.class)
    public void constructNullHost()
    {
        new ProxyConfig(null, -1, null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructNegativePort()
    {
        new ProxyConfig("localhost", -29, null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructPortOutOfRange()
    {
        new ProxyConfig("localhost", 65536, null, null);
    }

    @Test
    public void constructPortUnspecified()
    {
        new ProxyConfig("localhost", -1, null, null);
    }
    
}
