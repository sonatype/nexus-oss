package org.sonatype.nexus.integrationtests.nexus2692;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Before;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

import com.thoughtworks.xstream.XStream;

public class AbstractEvictTaskIt
    extends AbstractNexusIntegrationTest
{

    private static final long A_DAY = 24L * 60L * 60L * 1000L;

    private Map<String, Double> pathMap = new HashMap<String, Double>();

    private List<String> neverDeleteFiles = new ArrayList<String>();

    private File storageWorkDir;

    private File attributesWorkDir;

    @Before
    public void setupStorageAndAttributes()
        throws Exception
    {
        File workDir = new File( AbstractNexusIntegrationTest.nexusWorkDir );

        this.storageWorkDir = new File( workDir, "storage" );
        this.attributesWorkDir = new File( workDir, "proxy/attributes" );

        FileUtils.copyDirectoryStructure( this.getTestResourceAsFile( "storage/" ), storageWorkDir );
        FileUtils.copyDirectoryStructure( this.getTestResourceAsFile( "attributes/" ), attributesWorkDir );

        // now setup all the attributes
        File attributesInfo = this.getTestResourceAsFile( "attributes.info" );
        BufferedReader reader = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        XStream xstream = new XStream();
        xstream.alias( "file", DefaultStorageFileItem.class );
        xstream.alias( "collection", DefaultStorageCollectionItem.class );
        xstream.alias( "link", DefaultStorageLinkItem.class );

        long timestamp = System.currentTimeMillis();

        try
        {
            reader = new BufferedReader( new FileReader( attributesInfo ) );

            String line = reader.readLine();
            while ( line != null )
            {
                String[] parts = line.split( " " );
                String filePart = parts[0];
                long offset = (long) ( Double.parseDouble( parts[1] ) * A_DAY );

                // get the file
                File attributeFile = new File( attributesWorkDir, filePart );
                if ( attributeFile.isFile() )
                {
                    this.pathMap.put( filePart, Double.parseDouble( parts[1] ) );

                    fis = new FileInputStream( attributeFile );
                    StorageItem storageItem = (StorageItem) xstream.fromXML( fis );
                    IOUtil.close( fis );

                    if ( filePart.startsWith( "releases/" ) || filePart.startsWith( "releases-m1/" )
                        || filePart.startsWith( "public/" ) /*
                                                             * groups are not checked, so the hashes are left behind,
                                                             * see: NEXUS-3026
                                                             */|| filePart.startsWith( "snapshots/" )
                        || filePart.startsWith( "thirdparty/" ) || filePart.contains( ".meta" )
                        || filePart.contains( ".index" ) )
                    {
                        neverDeleteFiles.add( filePart );
                    }

                    // update it
                    long variation = ( 1258582311671l - storageItem.getLastRequested() ) + timestamp;
                    storageItem.setLastRequested( variation + offset );

                    // write it
                    fos = new FileOutputStream( attributeFile );
                    xstream.toXML( storageItem, fos );
                    IOUtil.close( fos );
                }

                line = reader.readLine();
            }
        }
        finally
        {
            IOUtil.close( fos );
            IOUtil.close( fis );
            IOUtil.close( reader );
        }
    }

    protected void runTask( int days, String repoId )
        throws Exception
    {
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( repoId );

        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( days ) );

        ScheduledServiceListResource task = TaskScheduleUtil.runTask(
            EvictUnusedItemsTaskDescriptor.ID,
            EvictUnusedItemsTaskDescriptor.ID,
            100,
            prop,
            age );

        Assert.assertNotNull( "Task did not finish.", task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
    }

    protected SortedSet<String> buildListOfExpectedFilesForAllRepos( int days )
    {
        SortedSet<String> expectedFiles = new TreeSet<String>();

        expectedFiles.addAll( this.getNeverDeleteFiles() );

        for ( Entry<String, Double> entry : this.getPathMap().entrySet() )
        {
            if ( entry.getValue() > ( days * -1 ) )
            {
                expectedFiles.add( entry.getKey() );
            }
        }

        List<String> expectedShadows = new ArrayList<String>();

        // loop once more to look for the shadows (NOTE: the shadow id must be in the format of targetId-*
        for ( String expectedFile : expectedFiles )
        {
            String prefix = expectedFile.substring( 0, expectedFile.indexOf( "/" ) ) + "-";
            String fileName = new File( expectedFile ).getName();

            for ( String originalFile : this.getPathMap().keySet() )
            {
                if ( originalFile.startsWith( prefix ) && originalFile.endsWith( fileName ) )
                {
                    expectedShadows.add( originalFile );
                    break;
                }
            }
        }

        expectedFiles.addAll( expectedShadows );

        return expectedFiles;
    }

    protected SortedSet<String> buildListOfExpectedFiles( int days, List<String> otherNotChangedRepoids )
    {
        SortedSet<String> expectedFiles = this.buildListOfExpectedFilesForAllRepos( days );

        for ( String path : this.getPathMap().keySet() )
        {
            String repoId = path.substring( 0, path.indexOf( "/" ) );
            if ( otherNotChangedRepoids.contains( repoId ) )
            {
                System.out.println( "found it:" + path );
                expectedFiles.add( path );
            }
        }
        return expectedFiles;
    }

    @SuppressWarnings( "unchecked" )
    protected void checkForEmptyDirectories()
        throws IOException
    {
        // make sure we don't have any empty directories
        Set<String> emptyDirectories = new HashSet<String>();

        SortedSet<String> resultDirectories = this.getDirectoryPaths(  this
            .getStorageWorkDir());
        for ( String itemPath : resultDirectories )
        {
            if ( itemPath.split( File.separator ).length != 1 )
            {
                File directory = new File( this.getStorageWorkDir(), itemPath );
                if ( directory.list().length == 0 )
                {
                    emptyDirectories.add( itemPath );
                }
            }
        }

        Assert.assertTrue( "Found empty directories: " + emptyDirectories, emptyDirectories.size() == 0 );
    }

    protected String prettyList( Set<String> list )
    {
        StringBuffer buffer = new StringBuffer();
        for ( String string : list )
        {
            buffer.append( string ).append( "\n" );
        }

        return buffer.toString();
    }

    @SuppressWarnings( "unchecked" )
    protected SortedSet<String> getFilePaths( File basedir )
        throws IOException
    {
        SortedSet<String> result = new TreeSet<String>();
        List<String> paths = FileUtils.getFileNames( this.getStorageWorkDir(), null, null, false, true );
        for ( String path : paths )
        {
            result.add( path.replaceAll( Pattern.quote( "\\" ), Pattern.quote( "/" ) ) );
        }
        return result;
    }

    @SuppressWarnings( "unchecked" )
    protected SortedSet<String> getDirectoryPaths( File basedir )
        throws IOException
    {
        SortedSet<String> result = new TreeSet<String>();
        List<String> paths = FileUtils.getDirectoryNames( this.getStorageWorkDir(), null, null, false, true );
        for ( String path : paths )
        {
            result.add( path.replaceAll( Pattern.quote( "\\" ), Pattern.quote( "/" ) ) );
        }
        return result;
    }

    public File getStorageWorkDir()
    {
        return storageWorkDir;
    }

    public File getAttributesWorkDir()
    {
        return attributesWorkDir;
    }

    public Map<String, Double> getPathMap()
    {
        return pathMap;
    }

    public List<String> getNeverDeleteFiles()
    {
        return neverDeleteFiles;
    }

}
