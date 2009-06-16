package org.sonatype.nexus.mock;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.pages.ServerTab;

public class ServerConfigTest
    extends SeleniumTest
{

    @Test
    public void requiredFields()
    {
        LoginTest.doLogin( main );

        ServerTab serverCfg = main.openServer();
        assertErrorText( serverCfg.getSmtpHost(), "127.0.0.1" );
        assertErrorText( serverCfg.getSmtpPort(), 1125 );
        assertErrorText( serverCfg.getSmtpEmail(), "admin@sonatype.org");

        assertErrorText( serverCfg.getGlobalTimeout(), 10);
        assertErrorText( serverCfg.getGlobalRetry(), 3);

        serverCfg.getSecurityRealms().removeAll();
        Assert.assertTrue( serverCfg.getSecurityRealms().hasErrorText( "Select one or more items" ) );
        serverCfg.getSecurityRealms().addAll();
        Assert.assertFalse( serverCfg.getSecurityRealms().hasErrorText( "Select one or more items" ) );

        assertErrorText( serverCfg.getSecurityAnonymousUsername(), "anonymous");
        assertErrorText( serverCfg.getSecurityAnonymousPassword(), "anonymous");

        assertErrorText( serverCfg.getApplicationBaseUrl(), "http://localhost:8081/nexus");
    }

    private void assertErrorText( TextField tf, String validText )
    {
        tf.type( "" );
        Assert.assertTrue( "Expected validation", tf.hasErrorText( "This field is required" ) );
        tf.type( validText );
        Assert.assertFalse( "Should pass validation", tf.hasErrorText( "This field is required" ) );
    }

    private void assertErrorText( TextField tf, int validValue )
    {
        assertErrorText( tf, String.valueOf( validValue ) );
    }

}
