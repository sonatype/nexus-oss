package org.sonatype.nexus.integrationtests.nexus1954;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractDeleteArtifactsIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public static void clean()
        throws Exception
    {
        cleanWorkDir();
    }

    protected static final String REPO_TEST_HARNESS_PROXY = "nexus-test-harness-proxy";

    private File artifact;

    private Gav artifact1v2;

    private Gav artifact1v1;

    private Gav artifact2v1;

    public AbstractDeleteArtifactsIT()
    {
        super();
    }

    public AbstractDeleteArtifactsIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    @Before
    public void init()
        throws IllegalArtifactCoordinateException
    {
        artifact = getTestFile( "artifact.jar" );
        artifact1v1 = GavUtil.newGav( "nexus1954", "artifact1", "1.0" );
        artifact1v2 = GavUtil.newGav( "nexus1954", "artifact1", "2.0" );
        artifact2v1 = GavUtil.newGav( "nexus1954", "artifact2", "1.0" );
    }

    @Test
    public void indexTest()
        throws Exception
    {
        updateIndexes();
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );

        DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, artifact1v2, artifact );
        DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_REPO, GavUtil.newGav( "nexus1954", "artifact2", "1.0" ),
                                            artifact );

        updateIndexes();
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v2, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact2v1, REPO_TEST_HARNESS_REPO ).size() );

        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v2, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact2v1, REPO_TEST_HARNESS_PROXY ).size() );

        deleteArtifact( artifact1v2 );

        updateIndexes();
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact1v2, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact2v1, REPO_TEST_HARNESS_REPO ).size() );

        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact1v2, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 1, SearchMessageUtil.searchFor( artifact2v1, REPO_TEST_HARNESS_PROXY ).size() );

        deleteArtifact( artifact1v1 );
        deleteArtifact( artifact2v1 );

        updateIndexes();
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact1v2, REPO_TEST_HARNESS_REPO ).size() );
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact2v1, REPO_TEST_HARNESS_REPO ).size() );

        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact1v1, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact1v2, REPO_TEST_HARNESS_PROXY ).size() );
        Assert.assertEquals( 0, SearchMessageUtil.searchFor( artifact2v1, REPO_TEST_HARNESS_PROXY ).size() );
    }

    private void deleteArtifact( Gav gav )
        throws FileNotFoundException, IOException
    {
        String dirPath = gav.getGroupId().replace( '.', '/' ) + "/" + gav.getArtifactId() + "/" + gav.getVersion();
        Assert.assertTrue( deleteFromRepository( REPO_TEST_HARNESS_REPO, dirPath ) );
    }

    private static final SimpleDateFormat df;

    static
    {
        df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
        df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    protected void updateIndexes()
        throws Exception
    {

        long hostedLastMod = -1;

        File hostedIndexProps =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO
                + "/.index/nexus-maven-repository-index.properties" );
        if ( hostedIndexProps.exists() )
        {
            hostedLastMod = readLastMod( hostedIndexProps );
        }

        long proxyLastMod = -1;
        File proxyIndexProps =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_REPO
                + "/.index/nexus-maven-repository-index.properties" );
        if ( proxyIndexProps.exists() )
        {
            proxyLastMod = readLastMod( proxyIndexProps );
        }

        TaskScheduleUtil.waitForAllTasksToStop();

        runUpdateIndex();

        long hostedLastMod2 = readLastMod( hostedIndexProps );
        Assert.assertTrue( hostedLastMod < hostedLastMod2 );

        long proxyLastMod2 = readLastMod( proxyIndexProps );
        Assert.assertTrue( proxyLastMod < proxyLastMod2 );
    }

    protected abstract void runUpdateIndex()
        throws Exception;

    private long readLastMod( File indexProps )
        throws Exception
    {
        Properties p = new Properties();
        InputStream input = new FileInputStream( indexProps );
        p.load( input );
        input.close();

        return df.parse( p.getProperty( "nexus.index.time" ) ).getTime();
    }

}