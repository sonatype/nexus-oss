package org.sonatype.nexus.security.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.events.EventInspector;

public class SecurityConfigurationUpgradeTest
    extends AbstractNexusTestCase
{

    protected void copySecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/org/sonatype/nexus/security/upgrade/security.xml", getNexusSecurityConfiguration() );
    }

    public void testLoadComponent() throws Exception
    {
        Assert.assertNotNull( this.lookup( EventInspector.class, "SecurityUpgradeEventInspector" ) );
    }

    public void testSecurityUpgradeAndEvent()
        throws Exception
    {
        this.copySecurityConfigToPlace();

        this.lookup( Nexus.class );

        // verify
        this.verifyUpgrade( "/org/sonatype/nexus/security/upgrade/security.result.xml" );

    }

    private void verifyUpgrade( String resource )
        throws IOException
    {
        InputStream stream = null;
        StringWriter writer = new StringWriter();
        try
        {
            stream = getClass().getResourceAsStream( resource );
            IOUtil.copy( stream, writer );
        }
        finally
        {
            IOUtil.close( stream );
        }

        String expected = writer.toString();

        // security should be upgraded now. lets look at the security.xml
        String securityXML = FileUtils.fileRead( getNexusSecurityConfiguration() );

        Assert.assertEquals( expected, securityXML );

    }

}
