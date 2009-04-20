package org.sonatype.nexus.proxy.repository;

import java.util.Arrays;

import junit.framework.Assert;

import org.jsecurity.SecurityUtils;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetRegistry;

public class AccessTest
    extends AbstractProxyTestEnvironment
{
    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

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

    public void testGroupAccess()
        throws NoSuchRepositoryException,
            StorageException,
            AccessDeniedException,
            IllegalOperationException,
            ItemNotFoundException
    {

        // user does not have access to the group
        try
        {
            this.getItem( "allrepo3", "test", "/spoof/simple.txt" );
            Assert.fail( "Expected AccessDeniedException" );
        }
        catch ( AccessDeniedException e )
        {
            // expected
        }

        // this user has access to the group
        StorageItem item = this.getItem( "alltest", "test", "/spoof/simple.txt" );
        // the first repo in the group
        Assert.assertEquals( "repo1", item.getRepositoryId() );

    }

    public void testRepositoryAccess()
        throws NoSuchRepositoryException,
            StorageException,
            AccessDeniedException,
            IllegalOperationException,
            ItemNotFoundException
    {
        // Not true, currently perms are always transitive
        // group access imples repository access, IF NOT EXPOSED, SO "UN"-EXPOSE IT
        // getRepositoryRegistry().getRepositoryWithFacet( "test", GroupRepository.class ).setExposed( false );

        StorageItem item = this.getItem( "alltest", "repo1", "/spoof/simple.txt" );
        Assert.assertEquals( "repo1", item.getRepositoryId() );
    }

    //
    // public void testAuthorizerDirectly()
    // throws Exception
    // {
    // String repoId = "repo1";
    // String path = "/spoof/simple.txt";
    // String username = "alltest";
    //
    // Subject subject = SecurityUtils.getSecurityManager().login( new UsernamePasswordToken( username, "" ) );
    //
    // NexusItemAuthorizer authorizer = this.lookup( NexusItemAuthorizer.class );
    //
    // ResourceStoreRequest request = new ResourceStoreRequest( "/repositories/" + repoId + "/" + path, true );
    // Assert.assertTrue( authorizer.authorizePath( request, Action.read ) );
    //
    // // not sure if we really need to log the user out, we are not using a remember me,
    // // but what can it hurt?
    // SecurityUtils.getSecurityManager().logout( subject.getPrincipals() );
    //
    // }

    private StorageItem getItem( String username, String repositoryId, String path )
        throws NoSuchRepositoryException,
            StorageException,
            AccessDeniedException,
            IllegalOperationException,
            ItemNotFoundException
    {
        Subject subject = SecurityUtils.getSecurityManager().login( new UsernamePasswordToken( username, "" ) );

        Repository repo = this.getRepositoryRegistry().getRepository( repositoryId );

        ResourceStoreRequest request = new ResourceStoreRequest( path, false );

        StorageItem item = repo.retrieveItem( request );

        // not sure if we really need to log the user out, we are not using a remember me,
        // but what can it hurt?
        SecurityUtils.getSecurityManager().logout( subject.getPrincipals() );

        return item;
    }

    private String targetMatchToString( TargetMatch targetMatch )
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "RepoId: " ).append( targetMatch.getRepository().getId() ).append( "\n" );
        Target target = targetMatch.getTarget();
        buffer.append( "target: " ).append( target.getId() ).append( " - " ).append( target.getName() ).append( "\n" );

        for ( String pattern : target.getPatternTexts() )
        {
            buffer.append( "\tpattern: " ).append( pattern );
        }

        return buffer.toString();
    }

    @Override
    public void setUp()
        throws Exception
    {
        ApplicationConfiguration applicationConfiguration = this.lookup( ApplicationConfiguration.class );
        applicationConfiguration.getConfiguration().getSecurity().setEnabled( true );
        applicationConfiguration.saveConfiguration();

        System.out.println( "ApplicationConfiguration (test): " + applicationConfiguration );

        super.setUp();

        TargetRegistry targetRegistry = this.lookup( TargetRegistry.class );

        Target t1 = new Target( "maven2-all", "All (Maven2)", new Maven2ContentClass(), Arrays
            .asList( new String[] { ".*" } ) );

        targetRegistry.addRepositoryTarget( t1 );

        // setup security
        PlexusSecurity securityManager = this.lookup( PlexusSecurity.class );
        SecurityUtils.setSecurityManager( securityManager );

    }

}
