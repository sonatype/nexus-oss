package org.sonatype.nexus.selenium.nexus2165;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.pages.ServerTab;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2165ServerConfigTest.class )
public class Nexus2165ServerConfigTest
    extends SeleniumTest
{

    @Test
    public void requiredFields()
    {
        doLogin();

        ServerTab serverCfg = main.openServer();
        assertErrorText( serverCfg.getSmtpHost(), "127.0.0.1" );
        assertErrorText( serverCfg.getSmtpPort(), 1125 );
        assertErrorText( serverCfg.getSmtpEmail(), "admin@sonatype.org" );

        assertErrorText( serverCfg.getGlobalTimeout(), 10 );
        assertErrorText( serverCfg.getGlobalRetry(), 3 );

        serverCfg.getSecurityRealms().removeAll();
        Assert.assertTrue( serverCfg.getSecurityRealms().hasErrorText( "Select one or more items" ) );
        serverCfg.getSecurityRealms().addAll();
        Assert.assertFalse( serverCfg.getSecurityRealms().hasErrorText( "Select one or more items" ) );

        assertErrorText( serverCfg.getSecurityAnonymousUsername(), "anonymous" );
        assertErrorText( serverCfg.getSecurityAnonymousPassword(), "anonymous" );

        assertErrorText( serverCfg.getApplicationBaseUrl(), "http://localhost:8081/nexus" );
    }

    private void assertErrorText( TextField tf, String validText )
    {
        tf.type( "" );
        assertTrue( "Expected validation", tf.hasErrorText( "This field is required" ) );
        tf.type( validText );
        assertFalse( "Should pass validation", tf.hasErrorText( "This field is required" ) );
    }

    private void assertErrorText( TextField tf, int validValue )
    {
        assertErrorText( tf, String.valueOf( validValue ) );
    }

}
