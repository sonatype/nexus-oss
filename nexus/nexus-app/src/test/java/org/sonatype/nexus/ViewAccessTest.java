package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.NexusItemInfo;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.security.WebSecurityUtil;
import org.sonatype.security.DefaultSecuritySystem;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

public class ViewAccessTest
    extends AbstractProxyTestEnvironment
{
    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    private SecuritySystem securitySystem;

    private FeedRecorder feedRecorder;

    private IndexerManager indexerManager;
    
    private NexusConfiguration nexusConfiguration;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        if ( this.jettyTestsuiteEnvironmentBuilder == null )
        {
            ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
            this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        }
        return this.jettyTestsuiteEnvironmentBuilder;
    }

    @Override
    public void setUp()
        throws Exception
    {
        nexusConfiguration = lookup( NexusConfiguration.class );
        nexusConfiguration.loadConfiguration();

        super.setUp();
        
        TargetRegistry targetRegistry = this.lookup( TargetRegistry.class );

        Target t1 =
            new Target( "maven2-all", "All (Maven2)", new Maven2ContentClass(), Arrays.asList( new String[] { ".*" } ) );

        targetRegistry.addRepositoryTarget( t1 );

        String resource = this.getClass().getName().replaceAll( "\\.", "\\/" ) + "-security-configuration.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource( resource );
        FileUtils.copyURLToFile( url, new File( CONF_HOME, "security-configuration.xml" ) );

        // setup security
        DefaultSecuritySystem securitySystem = (DefaultSecuritySystem) this.lookup( SecuritySystem.class );
        securitySystem.start(); // this will reaload the conf files

        this.securitySystem = securitySystem;

        this.feedRecorder = this.lookup( FeedRecorder.class );

        this.indexerManager = this.lookup( IndexerManager.class );
    }

    // feeds test!
    public void testFeedAccess()
        throws Exception
    {
        String repoId = "test";
        String artifactPath = "";
        String remoteUrl = null;
        String message = "Test event";

        String action = NexusArtifactEvent.ACTION_DEPLOYED;

        Map<String, Object> eventContext = new HashMap<String, Object>();

        NexusItemInfo itemInfo = new NexusItemInfo();
        itemInfo.setRemoteUrl( remoteUrl );
        itemInfo.setPath( artifactPath );
        itemInfo.setRepositoryId( repoId );

        NexusArtifactEvent event = new NexusArtifactEvent();
        event.setMessage( message );
        event.setNexusItemInfo( itemInfo );
        event.setAction( action );
        event.setEventContext( eventContext );
        event.setEventDate( new Date() );

        feedRecorder.addNexusArtifactEvent( event );

        // now lets see if we can get access to this new event
        NexusArtifactEvent lastEvent = this.readLastEvent( "alltest" );
        Assert.assertNotNull( lastEvent );
        Assert.assertEquals( artifactPath, lastEvent.getNexusItemInfo().getPath() );
        Assert.assertEquals( repoId, lastEvent.getNexusItemInfo().getRepositoryId() );

        // try with a user without the view priv (it should work)
        lastEvent = this.readLastEvent( "test-noview" );
        Assert.assertNotNull( lastEvent );
        Assert.assertEquals( artifactPath, lastEvent.getNexusItemInfo().getPath() );
        Assert.assertEquals( repoId, lastEvent.getNexusItemInfo().getRepositoryId() );

        // now try with a user with no target priv
        lastEvent = this.readLastEvent( "wrong-target" );
        Assert.assertNull( lastEvent );
    }

    private NexusArtifactEvent readLastEvent( String username )
        throws AuthenticationException
    {
        // login
        WebSecurityUtil.setupWebContext( username + "-feed" );
        Subject subject = securitySystem.login( new UsernamePasswordToken( username, "" ) );

        List<NexusArtifactEvent> events =
            this.feedRecorder.getNexusArtifactEvents(
                                                      new HashSet<String>(
                                                                           Arrays.asList( new String[] { NexusArtifactEvent.ACTION_DEPLOYED } ) ),
                                                      0l, 1, null );

        // logout
        securitySystem.logout( subject.getPrincipals() );

        // we should only have 1 (at best)
        if ( events != null && !events.isEmpty() )
        {
            return events.get( 0 );
        }

        return null;
    }

    // search tests!
    public void testSearch()
        throws NoSuchRepositoryException, IOException, AuthenticationException, IllegalArtifactCoordinateException
    {
        String repoId = "test";
        String artifactId = "foo";
        String groupId = "bar";
        String version = "1.0.0";
        String classifier = null;
        String extention = "jar";

        ArtifactInfo artifactInfo = new ArtifactInfo( repoId, groupId, artifactId, version, classifier );
        ArtifactContext artifactContext =
            new ArtifactContext( null, null, null, artifactInfo, new Gav( groupId, artifactId, version, classifier,
                                                                          extention, null, null, null, false, false,
                                                                          null, false, null ) );

        this.indexerManager.addRepositoryIndexContext( repoId );
        IndexingContext context = this.indexerManager.getRepositoryLocalIndexContext( repoId );
        this.indexerManager.getNexusIndexer().addArtifactToIndex( artifactContext, context );

        ArtifactInfo resultArtifact = this.searchForSingleArtifact( "alltest", artifactId, repoId );
        Assert.assertNotNull( resultArtifact );
        Assert.assertEquals( artifactId, resultArtifact.artifactId );

        // now try with a user with no access
        resultArtifact = this.searchForSingleArtifact( "wrong-target", artifactId, repoId );
        Assert.assertNull( resultArtifact );

        // and now for a user with the target priv but no view
        resultArtifact = this.searchForSingleArtifact( "test-noview", artifactId, repoId );
        Assert.assertNotNull( resultArtifact );
        Assert.assertEquals( artifactId, resultArtifact.artifactId );

    }

    private ArtifactInfo searchForSingleArtifact( String username, String artifactId, String repositoryId )
        throws AuthenticationException, NoSuchRepositoryException
    {
        // login
        WebSecurityUtil.setupWebContext( username + "-" + repositoryId );
        Subject subject = securitySystem.login( new UsernamePasswordToken( username, "" ) );

        FlatSearchResponse searchResult =
            indexerManager.searchArtifactFlat( artifactId, repositoryId, new Integer( 0 ), new Integer( 1 ) );

        searchResult.getResults();

        // logout
        securitySystem.logout( subject.getPrincipals() );

        if ( !searchResult.getResults().isEmpty() )
        {
            return searchResult.getResults().iterator().next();
        }

        return null;

    }

    @Override
    public void tearDown()
        throws Exception
    {
        this.indexerManager.shutdown( true );

        super.tearDown();

        FileUtils.forceDelete( PLEXUS_HOME );
    }
}
