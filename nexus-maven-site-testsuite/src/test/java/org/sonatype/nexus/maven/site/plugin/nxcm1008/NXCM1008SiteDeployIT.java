/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.maven.site.plugin.nxcm1008;

import java.io.File;
import java.net.URL;

import org.junit.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.Test;

public class NXCM1008SiteDeployIT
    extends AbstractMavenNexusIT
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();

        repo.setKey( "repositoryId" );

        repo.setValue( "fake-central" );

        TaskScheduleUtil.runTask( "RebuildMavenMetadata-Nexus1022", RebuildMavenMetadataTaskDescriptor.ID, repo );
    }

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
            verifier1.setLogFileName( "logs/maven-execution/nxcm1008/site-1.log" ); // TODO: hardwired as Nexus 2.1.1 ITs still suffer from double preprending log path

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

        File downloadedFile =
            this.downloadFile( new URL( TestProperties.getString( "nexus.base.url" )
                + "content/sites/nxcm1008site/site-1/index.html" ), "target/site/tmp-index.html" );
        FileTestingUtils.compareFileSHA1s( downloadedFile,
                                           new File( "target/resources/nxcm1008/files/site-1/target/site/index.html" ) );
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

        RepositoryMessageUtil messageUtil =
            new RepositoryMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        // this also validates
        // TODO: No, it's not: it uses RepositoryTypeRegistry from _this_ plexus container,
        // while Nexus (where the new repo type is registered) runs in Guice!
        RepositoryResource result = (RepositoryResource) messageUtil.createRepository( resource, false );

        Assert.assertEquals( "maven-site", result.getProvider() );
        Assert.assertEquals( WebSiteRepository.class.getName(), result.getProviderRole() ); // not passed back
    }

}
