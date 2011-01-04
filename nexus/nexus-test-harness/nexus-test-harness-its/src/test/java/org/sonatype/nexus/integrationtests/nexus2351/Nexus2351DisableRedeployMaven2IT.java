/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus2351;

import java.io.File;

import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus2351DisableRedeployMaven2IT
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil repoUtil = null;

    private File artifact;

    private File artifactMD5;

    @BeforeMethod
    public void setup()
        throws Exception
    {
        artifact = this.getTestFile( "artifact.jar" );
        artifactMD5 = this.getTestFile( "artifact.jar.md5" );
    }

    public Nexus2351DisableRedeployMaven2IT()
    {

    }

    @BeforeClass
    public void init()
        throws ComponentLookupException
    {
        this.repoUtil = new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void testM2ReleaseAllowRedeploy()
        throws Exception
    {

        String repoId = this.getTestId() + "-testM2ReleaseAllowRedeploy";

        this.createM2Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE, RepositoryPolicy.RELEASE );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseAllowRedeploy-1.0.0.jar" );
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseAllowRedeploy-1.0.0.jar" );
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseAllowRedeploy-1.0.0.jar" );

        // now test checksums
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseAllowRedeploy-1.0.0.jar.md5" );
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseAllowRedeploy-1.0.0.jar.md5" );
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseAllowRedeploy-1.0.0.jar.md5" );
    }

    @Test
    public void testM2ReleaseNoRedeploy()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM2ReleaseNoRedeploy";

        this.createM2Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE_ONCE, RepositoryPolicy.RELEASE );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2ReleaseNoRedeploy/1.0.0/testM2ReleaseNoRedeploy-1.0.0.jar" );

        // checksum should work
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2ReleaseNoRedeploy/1.0.0/testM2ReleaseNoRedeploy-1.0.0.jar.md5" );

        try
        {
            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2ReleaseNoRedeploy/1.0.0/testM2ReleaseNoRedeploy-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        try
        {
            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2ReleaseNoRedeploy/1.0.0/testM2ReleaseNoRedeploy-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        try
        {
            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
                "testM2Repo/group/testM2ReleaseNoRedeploy/1.0.0/testM2ReleaseNoRedeploy-1.0.0.jar.md5" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }
    }

    @Test
    public void testM2ReleaseNoRedeployMultipleVersions()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM2ReleaseNoRedeployMultipleVersions";

        this.createM2Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE_ONCE, RepositoryPolicy.RELEASE );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2ReleaseNoRedeployMultipleVersions/1.0.0/testM2ReleaseNoRedeployMultipleVersions-1.0.0.jar" );

        try
        {
            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2ReleaseNoRedeployMultipleVersions/1.0.0/testM2ReleaseNoRedeployMultipleVersions-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2ReleaseNoRedeployMultipleVersions/1.0.1/testM2ReleaseNoRedeployMultipleVersions-1.0.1.jar" );

        try
        {
            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2ReleaseNoRedeployMultipleVersions/1.0.1/testM2ReleaseNoRedeployMultipleVersions-1.0.1.jar" );
            Assert.fail( "expected TransferFailedException" );
        }
        catch ( TransferFailedException e )
        {
            // expected
        }

    }

    @Test
    public void testM2ReleaseReadOnly()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM2ReleaseReadOnly";

        this.createM2Repo( repoId, RepositoryWritePolicy.READ_ONLY, RepositoryPolicy.RELEASE );

        try
        {

            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2ReleaseReadOnly/1.0.0/testM2ReleaseReadOnly-1.0.0.jar" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }

        try
        {

            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
                "testM2Repo/group/testM2ReleaseAllowRedeploy/1.0.0/testM2ReleaseReadOnly-1.0.0.jar.md5" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }

    }

    @Test
    public void testM2SnapshotAllowRedeploy()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM2SnapshotAllowRedeploy";

        this.createM2Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE, RepositoryPolicy.SNAPSHOT );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-20090729.054915-216.jar" );
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-20090729.054915-217.jar" );
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-20090729.054915-218.jar" );

        // now for the MD5
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-20090729.054915-217.jar.md5" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-20090729.054915-218.jar.md5" );

        // now for just the -SNAPSHOT

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-SNAPSHOT.jar" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-SNAPSHOT.jar" );

        // MD5
        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-SNAPSHOT.jar.md5" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2SnapshotAllowRedeploy/1.0.0-SNAPSHOT/testM2SnapshotAllowRedeploy-SNAPSHOT.jar.md5" );

    }

    @Test
    public void testM2SnapshotNoRedeploy()
        throws Exception
    {
        String repoId = this.getTestId() + "-testM2SnapshotNoRedeploy";

        this.createM2Repo( repoId, RepositoryWritePolicy.ALLOW_WRITE_ONCE, RepositoryPolicy.SNAPSHOT );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-20090729.054915-218.jar" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-20090729.054915-219.jar" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-20090729.054915-220.jar" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-SNAPSHOT.jar" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-SNAPSHOT.jar" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-SNAPSHOT.jar.md5" );

        getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
            "testM2Repo/group/testM2SnapshotNoRedeploy/1.0.0-SNAPSHOT/testM2SnapshotNoRedeploy-SNAPSHOT.jar.md5" );
    }

    @Test
    public void testM2SnapshotReadOnly()
        throws Exception
    {

        String repoId = this.getTestId() + "-testM2SnapshotReadOnly";

        this.createM2Repo( repoId, RepositoryWritePolicy.READ_ONLY, RepositoryPolicy.SNAPSHOT );

        try
        {

            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2SnapshotReadOnly/1.0.0-SNAPSHOT/testM2SnapshotReadOnly-20090729.054915-218.jar" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
        try
        {

            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
                "testM2Repo/group/testM2SnapshotReadOnly/1.0.0-SNAPSHOT/testM2SnapshotReadOnly-20090729.054915-218.jar.md5" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
        try
        {

            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifactMD5,
                "testM2Repo/group/testM2SnapshotReadOnly/1.0.0-SNAPSHOT/testM2SnapshotReadOnly-SNAPSHOT.jar.md5" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
        try
        {

            getDeployUtils().deployWithWagon( "http", this.getRepositoryUrl( repoId ), artifact,
                "testM2Repo/group/testM2SnapshotReadOnly/1.0.0-SNAPSHOT/testM2SnapshotReadOnly-SNAPSHOT.jar" );
            Assert.fail( "expected TransferFailedException" );

        }
        catch ( TransferFailedException e )
        {
            // expected
        }
    }

    private void createM2Repo( String repoId, RepositoryWritePolicy writePolicy, RepositoryPolicy releasePolicy )
        throws Exception
    {
        RepositoryResource repo = new RepositoryResource();

        repo.setId( repoId );
        repo.setBrowseable( true );
        repo.setExposed( true );
        repo.setRepoType( "hosted" );
        repo.setName( repoId );
        repo.setRepoPolicy( releasePolicy.name() );
        repo.setWritePolicy( writePolicy.name() );
        repo.setProvider( "maven2" );
        repo.setFormat( "maven2" );
        repo.setIndexable( false );

        this.repoUtil.createRepository( repo );
    }

}
