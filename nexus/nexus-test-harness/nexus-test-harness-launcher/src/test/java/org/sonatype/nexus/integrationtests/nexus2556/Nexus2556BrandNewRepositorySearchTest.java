package org.sonatype.nexus.integrationtests.nexus2556;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;

public class Nexus2556BrandNewRepositorySearchTest
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil repoUtil;

    public Nexus2556BrandNewRepositorySearchTest()
        throws ComponentLookupException
    {
        this.repoUtil =
            new RepositoryMessageUtil( XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML,
                                       getRepositoryTypeRegistry() );
    }

    @Test
    public void hostedTest()
        throws IOException, Exception
    {
        String repoId = "nexus2556-hosted";
        RepositoryResource repo = new RepositoryResource();
        repo.setProvider( "maven2" );
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( "release" );
        repo.setChecksumPolicy( "ignore" );
        repo.setBrowseable( false );

        repo.setId( repoId );
        repo.setName( repoId );
        repo.setRepoType( "hosted" );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo.setDownloadRemoteIndexes( true );
        repo.setBrowseable( true );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo.setChecksumPolicy( ChecksumPolicy.IGNORE.name() );

        repo.setIndexable( true ); // being sure!!!
        repoUtil.createRepository( repo );

        repo = (RepositoryResource) repoUtil.getRepository( repoId );
        Assert.assertTrue( repo.isIndexable() );

        Gav gav = GavUtil.newGav( "nexus2556", "artifact", "1.0" );
        DeployUtils.deployUsingGavWithRest( repoId, gav, getTestFile( "artifact.jar" ) );

        List<NexusArtifact> result = SearchMessageUtil.searchFor( gav, repoId );
        Assert.assertEquals( "Results: \n" + XStreamFactory.getXmlXStream().toXML( result ), 1, result.size() );

        result = SearchMessageUtil.searchFor( Collections.singletonMap( "q", "nexus2556" ), repoId );
        Assert.assertEquals( "Results: \n" + XStreamFactory.getXmlXStream().toXML( result ), 1, result.size() );
    }

}
