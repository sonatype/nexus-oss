package org.sonatype.nexus.proxy.storage.remote;

import org.junit.Assert;
import org.junit.Test;

import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

public class RemoteProviderHintFactoryTest
    extends PlexusTestCaseSupport
{
    private static final String FAKE_VALUE = "Foo-Bar";

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        // clear the property
        System.clearProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY );
    }

    @Test
    public void testIt()
        throws Exception
    {
        RemoteProviderHintFactory hintFactory = this.lookup( RemoteProviderHintFactory.class );

        // clear the property
        System.clearProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY );

        // nothing set
        Assert.assertEquals( CommonsHttpClientRemoteStorage.PROVIDER_STRING, hintFactory.getDefaultHttpRoleHint() );

        System.setProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY, FAKE_VALUE );
        Assert.assertEquals( FAKE_VALUE, hintFactory.getDefaultHttpRoleHint() );
    }
}
