package com.sonatype.nexus.proxy.maven.site.nxcm1008;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

public class NXCM1008SiteDeployIT
    extends AbstractMavenNexusIT
{

    @Test
    public void testDeployAndGet()
        throws Exception
    {
        this.setTestRepositoryId( "nxcm1008site" );
        this.createSiteRepo();
        
        // now deploy something
        
        File mavenProject1 = getTestFile( "site-1" );

        File settings1 = getTestFile( "settings1.xml" );

        Verifier verifier1 = null;

        try
        {
            verifier1 = createVerifier( mavenProject1, settings1 );

            verifier1.setAutoclean( false );

            verifier1.executeGoal( "site" );
            verifier1.verifyErrorFreeLog();
            
            verifier1.executeGoal( "site:deploy" );
            verifier1.verifyErrorFreeLog();
        }

        catch ( VerificationException e )
        {
            e.printStackTrace();
            failTest( verifier1 );
        }
        
        // now call get and compare it with the original
        
        File downloadedFile = this.downloadFile( new URL( TestProperties.getString( "nexus.base.url" ) +"content/sites/nxcm1008site/site-1/index.html") , "target/site/tmp-index.html" );
        FileTestingUtils.compareFileSHA1s( downloadedFile, new File( "target/resources/nxcm1008/files/site-1/target/site/index.html" ) );
    }
    
    private void createSiteRepo()
        throws Exception
    {
        RepositoryResource resource = new RepositoryResource();

        resource.setId( this.getTestRepositoryId() );
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( this.getTestRepositoryId() + "-Name" );
        resource.setProviderRole( WebSiteRepository.class.getName() );
        resource.setProvider( "maven-site" );
        resource.setRepoPolicy( RepositoryPolicy.MIXED.name() ); // TODO: this shouldn't be required
        resource.setBrowseable( true ); 
        resource.setExposed( true ); // sort of important for a website
        resource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        resource.setChecksumPolicy( ChecksumPolicy.WARN.name() );

        RepositoryMessageUtil messageUtil = new RepositoryMessageUtil(
            this.getJsonXStream(),
            MediaType.APPLICATION_JSON,
            getRepositoryTypeRegistry() );
        // this also validates
        RepositoryResource result = (RepositoryResource) messageUtil.createRepository( resource );

        Assert.assertEquals( "maven-site", result.getProvider() );
        Assert.assertEquals( WebSiteRepository.class.getName(), result.getProviderRole() ); // not passed back
    }

}
