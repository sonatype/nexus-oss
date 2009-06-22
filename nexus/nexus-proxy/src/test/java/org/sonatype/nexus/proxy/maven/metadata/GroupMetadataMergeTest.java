package org.sonatype.nexus.proxy.maven.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.codehaus.plexus.digest.Md5Digester;
import org.codehaus.plexus.digest.Sha1Digester;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

public class GroupMetadataMergeTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );

        return jettyTestsuiteEnvironmentBuilder;
    }

    public void testGMerge()
        throws Exception
    {
        String mdPath = "/md-merge/g/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( 4, md.getPlugins().size() );
        assertEquals( "core-it", ( (Plugin) md.getPlugins().get( 0 ) ).getPrefix() );
        assertEquals( "resources", ( (Plugin) md.getPlugins().get( 1 ) ).getPrefix() );
        assertEquals( "site", ( (Plugin) md.getPlugins().get( 2 ) ).getPrefix() );
        assertEquals( "surefire-report", ( (Plugin) md.getPlugins().get( 3 ) ).getPrefix() );
    }

    public void testGAMerge()
        throws Exception
    {
        String mdPath = "/md-merge/ga/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );

        assertEquals( "1.4.0-SNAPSHOT", md.getVersioning().getLatest() );
        assertEquals( "1.3.4", md.getVersioning().getRelease() );
        String[] versions = {
            "1.2.1",
            "1.3.0",
            "1.3.1-SNAPSHOT",
            "1.3.1",
            "1.3.2",
            "1.3.3-SNAPSHOT",
            "1.3.3",
            "1.3.4",
            "1.4.0-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20090620231210", md.getVersioning().getLastUpdated() );
    }

    /**
     * Merge 3 GA maven-metadata.xml
     * 
     * @throws Exception
     */
    public void testGA3Merge()
        throws Exception
    {
        String mdPath = "/md-merge/ga3/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );

        assertEquals( "1.4.1-SNAPSHOT", md.getVersioning().getLatest() );
        assertEquals( "1.3.4", md.getVersioning().getRelease() );
        String[] versions = {
            "1.2.1",
            "1.3.0",
            "1.3.1-SNAPSHOT",
            "1.3.1",
            "1.3.2",
            "1.3.3-SNAPSHOT",
            "1.3.3",
            "1.3.4",
            "1.4.0-SNAPSHOT",
            "1.4.1-SNAPSHOT" };
        assertEquals( Arrays.asList( versions ), md.getVersioning().getVersions() );
        assertEquals( "20090720231210", md.getVersioning().getLastUpdated() );
    }

    public void testGAVMerge()
        throws Exception
    {
        String mdPath = "/md-merge/gav/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        Metadata md = parseMetadata( (StorageFileItem) item );

        assertEquals( "org.sonatype.nexus", md.getGroupId() );
        assertEquals( "nexus", md.getArtifactId() );
        assertEquals( "1.3.4-SNAPSHOT", md.getVersion() );
        assertEquals( "20090527.162714", md.getVersioning().getSnapshot().getTimestamp() );
        assertEquals( 51, md.getVersioning().getSnapshot().getBuildNumber() );
        assertEquals( "20090527162714", md.getVersioning().getLastUpdated() );
    }

    public void testChecksum()
        throws Exception
    {
        String mdPath = "/md-merge/checksum/maven-metadata.xml";

        StorageItem item = getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );

        File mdFile = File.createTempFile( "metadata", "tmp" );
        saveItemToFile( ( (StorageFileItem) item ), mdFile );

        StorageItem md5Item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/groups/test" + mdPath + ".md5", false ) );
        StorageItem sha1Item = getRootRouter().retrieveItem(
            new ResourceStoreRequest( "/groups/test" + mdPath + ".sha1", false ) );

        String md5Hash = contentAsString( md5Item );
        String sha1Hash = contentAsString( sha1Item );

        Md5Digester md5Digester = new Md5Digester();
        md5Digester.verify( mdFile, md5Hash );
        Sha1Digester sha1Digester = new Sha1Digester();
        sha1Digester.verify( mdFile, sha1Hash );
    }

    public void testConflictMerge()
        throws Exception
    {
        String mdPath = "/md-merge/conflict/maven-metadata.xml";

        try
        {
            getRootRouter().retrieveItem( new ResourceStoreRequest( "/groups/test" + mdPath, false ) );

            fail( "Should not be able to retrieve the maven-metadata.xml, since the merge should fail caused by incompatible artifactId." );
        }
        catch ( StorageException e )
        {
            getLogger().info( e.getMessage() );
            getLogger().info( e.getCause().getMessage() );
        }
    }

    protected Metadata parseMetadata( File file )
        throws Exception
    {
        InputStream in = null;

        try
        {
            in = new FileInputStream( file );

            return MetadataBuilder.read( in );
        }
        finally
        {
            if ( in != null )
            {
                in.close();
            }
        }
    }

    protected Metadata parseMetadata( StorageFileItem item )
        throws Exception
    {
        InputStream in = null;

        try
        {
            in = item.getInputStream();

            return MetadataBuilder.read( in );
        }
        finally
        {
            if ( in != null )
            {
                in.close();
            }
        }
    }
}
