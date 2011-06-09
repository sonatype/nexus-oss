package org.sonatype.nexus.plugins.p2.repository.its.nxcm2838;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;

public class NXCM2838MergedMetadataP2GroupIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public NXCM2838MergedMetadataP2GroupIT()
    {
        super( "nxcm2838" );
    }

    @Test
    public void test()
        throws Exception
    {
        final File artifactsXmlFile = new File( "target/downloads/nxcm2838/artifacts.xml" );
        Assert.assertFalse( artifactsXmlFile.exists() );

        downloadFile( new URL( getGroupUrl( getTestRepositoryId() ) + "artifacts.xml" ),
            artifactsXmlFile.getAbsolutePath() );
        Assert.assertTrue( artifactsXmlFile.exists() );

        final String artifactsXmlContent = FileUtils.fileRead( artifactsXmlFile );

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
