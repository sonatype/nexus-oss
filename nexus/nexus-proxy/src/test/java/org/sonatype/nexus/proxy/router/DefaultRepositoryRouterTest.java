package org.sonatype.nexus.proxy.router;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.Subject;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.maven1.M1LayoutedM2ShadowRepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.maven1.M1Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2GroupRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2LayoutedM1ShadowRepository;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.nexus.security.WebSecurityUtil;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;

public class DefaultRepositoryRouterTest
    extends AbstractNexusTestCase
{
    private RepositoryRouter router = null;

    private RepositoryRegistry repositoryRegistry = null;

    private SecuritySystem securitySystem = null;

    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );
        ctx.put( "security-xml-file", new File( CONF_HOME, "security.xml" ).getAbsolutePath() );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        ApplicationConfiguration applicationConfiguration = this.lookup( ApplicationConfiguration.class );
        applicationConfiguration.saveConfiguration();

        super.setUp();

        this.router = this.lookup( RepositoryRouter.class );
        this.repositoryRegistry = this.lookup( RepositoryRegistry.class );

        this.buildRepository( "repo1" );
        this.buildRepository( "repo2" );

        this.buildGroupRepository( "group1" );
        this.buildGroupRepository( "group2" );

        this.buildShadowRepository( "repo3" );
        this.buildShadowRepository( "repo4" );

        // copy the security-configuration
        String resource = this.getClass().getName().replaceAll( "\\.", "\\/" ) + "-security-configuration.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource( resource );
        FileUtils.copyURLToFile( url, new File( CONF_HOME, "security-configuration.xml" ) );

        // create a target
        TargetRegistry targetRegistry = this.lookup( TargetRegistry.class );
        Target t1 = new Target( "maven2-all", "All (Maven2)", new Maven2ContentClass(), Arrays
            .asList( new String[] { ".*" } ) );
        targetRegistry.addRepositoryTarget( t1 );
        
        // setup security
        this.securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.setSecurityEnabled( true );

    }

    public void testRouterWithViewAccess()
        throws Exception
    {
        Subject subject = this.loginUser( "repo1user" );

        ResourceStoreRequest request = new ResourceStoreRequest( "/repositories/" );

        StorageItem item = router.retrieveItem( request );

        StorageCollectionItem collectionItem = (StorageCollectionItem) item;

        // this user only has access to repo1, that is all they should see
        Assert.assertEquals( "User should only have access to 'repo1'", 1, collectionItem.list().size() );
        Assert.assertEquals( "repo1", collectionItem.list().iterator().next().getName() );
        
        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }
    
    public void testRouterWithNoViewAccess()
    throws Exception
    {
        Subject subject = this.loginUser( "repo1userNoView" );
    
        ResourceStoreRequest request = new ResourceStoreRequest( "/repositories/" );
    
        StorageItem item = router.retrieveItem( request );
    
        StorageCollectionItem collectionItem = (StorageCollectionItem) item;
    
        // this user only has access to repo1, that is all they should see
        Assert.assertEquals( "User should not have access to any repos", 0, collectionItem.list().size() );

        // logout user
        this.securitySystem.logout( subject.getPrincipals() );
    }

    private Repository buildRepository( String repoId )
        throws Exception
    {
        M2Repository repo = (M2Repository) this.lookup( Repository.class, "maven2" );
        CRepository repoConfig = new DefaultCRepository();
        repoConfig.setId( repoId );
        repo.configure( repoConfig );
        this.repositoryRegistry.addRepository( repo );

        return repo;
    }

    private Repository buildGroupRepository( String repoId )
        throws Exception
    {
        M2GroupRepository repo = (M2GroupRepository) this.lookup( GroupRepository.class, "maven2" );
        CRepository repoConfig = new DefaultCRepository();
        repoConfig.setId( repoId );
        repo.configure( repoConfig );
        this.repositoryRegistry.addRepository( repo );

        return repo;
    }

    private Repository buildShadowRepository( String repoId )
        throws Exception
    {
        M1Repository repo = (M1Repository) this.lookup( Repository.class, "maven1" );
        CRepository repoConfig = new DefaultCRepository();
        repoConfig.setId( repoId );
        repo.configure( repoConfig );
        this.repositoryRegistry.addRepository( repo );

        // now for the shadow
        M2LayoutedM1ShadowRepository shadow = (M2LayoutedM1ShadowRepository) this.lookup(
            ShadowRepository.class,
            "m1-m2-shadow" );
        CRepository shadowConfig = new DefaultCRepository();
        shadowConfig.setId( repoId + "-shadow" );
        shadowConfig.setProviderRole( ShadowRepository.class.getName() );
        shadowConfig.setProviderHint( "m2-m1-shadow" );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        shadowConfig.setExternalConfiguration( exRepo );
        M1LayoutedM2ShadowRepositoryConfiguration exRepoConf = new M1LayoutedM2ShadowRepositoryConfiguration( exRepo );
        exRepoConf.setMasterRepositoryId( repo.getId() );
        exRepoConf.applyChanges();

        shadow.configure( shadowConfig );
        // shadow.
        this.repositoryRegistry.addRepository( shadow );

        return repo;

    }

    private Subject loginUser( String username )
        throws AuthenticationException
    {
        WebSecurityUtil.setupWebContext( username );
        return this.securitySystem.login( new UsernamePasswordToken( username, "" ) );
    }

}
