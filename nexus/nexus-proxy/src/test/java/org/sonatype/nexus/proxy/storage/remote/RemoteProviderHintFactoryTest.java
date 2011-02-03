package org.sonatype.nexus.proxy.storage.remote;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

public class RemoteProviderHintFactoryTest
    extends PlexusTestCase
{
    private static final String FAKE_VALUE = "Foo-Bar";
    
    public void testIt() throws Exception
    {
        RemoteProviderHintFactory hintFactory = this.lookup( RemoteProviderHintFactory.class );
        
        // clear the property
        System.clearProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY );
        
        // nothing set
        Assert.assertEquals( CommonsHttpClientRemoteStorage.PROVIDER_STRING, hintFactory.getDefaultRoleHint() );
        
        System.setProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY, FAKE_VALUE );
        Assert.assertEquals( FAKE_VALUE, hintFactory.getDefaultRoleHint() );
    }
}
