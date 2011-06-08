package com.sonatype.nexus.proxy.p2.its.nxcm2838;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.sonatype.nexus.proxy.p2.its.AbstractNexusProxyP2IntegrationIT;

public class NXCM2838MergedMetadataP2GroupIT
    extends AbstractNexusProxyP2IntegrationIT
{
    private static final String P2_GROUP_REPO = "p2group";

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );

        // to stop creating the proxies/lineups with no onboarding plugin
        System.setProperty( "p2.lineups.create", "false" );
    }

    @Test
    public void testNXCM2838MergedMetadataP2GroupIT()
        throws Exception
    {
        File artifactsXmlFile = new File( "target/downloads/NXCM2838MergedMetadataP2GroupIT/artifacts.xml" );
        Assert.assertFalse( artifactsXmlFile.exists() );

        downloadFile( new URL( getRepositoryUrl( P2_GROUP_REPO ) + "artifacts.xml" ),
            artifactsXmlFile.getAbsolutePath() );
        Assert.assertTrue( artifactsXmlFile.exists() );

        String artifactsXmlContent = FileUtils.fileRead( artifactsXmlFile );

        // for easer debug
        // System.out.println( "*****" );
        // System.out.println( artifactsXmlContent );

        // has 5 mappings
        Assert.assertTrue( artifactsXmlContent.contains( "<mappings size=\"5\">" ) );
        // has packed rule for bundles
        Assert.assertTrue( artifactsXmlContent.indexOf( "(&amp; (classifier=osgi.bundle) (format=packed))" ) > -1 );
        // has non-packed ruls for bundles
        Assert.assertTrue( artifactsXmlContent.indexOf( "(&amp; (classifier=osgi.bundle))" ) > -1 );
        // packed is before non-packed
        Assert.assertTrue( artifactsXmlContent.indexOf( "(&amp; (classifier=osgi.bundle) (format=packed))" ) < artifactsXmlContent.indexOf( "(&amp; (classifier=osgi.bundle))" ) );
    }
}
