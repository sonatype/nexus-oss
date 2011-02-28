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
package org.sonatype.nexus.integrationtests.nexus3615;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResource;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResourceRespose;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

/**
 * Tests the ?describe=maven2 content view.
 * 
 * @author Brian Demers
 */
public class Nexus3615MavenInfoIT
    extends AbstractNexusIntegrationTest
{

    /**
     * Positive release tests.
     * 
     * @throws Exception
     */
    @Test
    public void deployAndRunReleaseTests()
        throws Exception
    {
        // deploy releases
        Gav simpleJarGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1", null, "jar", null, null, null, false, null, false, null );
        deployGav( simpleJarGav, getTestRepositoryId() );
        downloadAndVerify( simpleJarGav, getTestRepositoryId() );

        Gav withClassifierGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1", "classifier", "jar", null, null, null, false, null,
                false, null );
        deployGav( withClassifierGav, getTestRepositoryId() );
        downloadAndVerify( withClassifierGav, getTestRepositoryId() );

        Gav withExtentionGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1", null, "extention", null, null, null, false, null, false,
                null );
        deployGav( withExtentionGav, getTestRepositoryId() );
        downloadAndVerify( withExtentionGav, getTestRepositoryId() );

        Gav withClassifierAndExtentionGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1", "classifier", "extention", null, null, null, false,
                null, false, null );
        deployGav( withClassifierAndExtentionGav, getTestRepositoryId() );
        downloadAndVerify( withClassifierAndExtentionGav, getTestRepositoryId() );
    }

    /**
     * Positive release tests.
     * 
     * @throws Exception
     */
    @Test
    public void deployAndRunSnapshotTests()
        throws Exception
    {
        // deploy releases
        Gav simpleJarGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1-SNAPSHOT", null, "jar", 1, System.currentTimeMillis(), null,
                false, null, false, null );
        deployGav( simpleJarGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        downloadAndVerify( simpleJarGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );

        Gav withClassifierGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1-SNAPSHOT", "classifier", "jar", 2, System.currentTimeMillis(),
                null, false, null, false, null );
        deployGav( withClassifierGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        downloadAndVerify( withClassifierGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );

        Gav withExtentionGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1-SNAPSHOT", null, "extention", 3, System.currentTimeMillis(),
                null, false, null, false, null );
        deployGav( withExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        downloadAndVerify( withExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );

        Gav withClassifierAndExtentionGav =
            new Gav( "nexus3615", "simpleJar", "1.0.1-SNAPSHOT", "classifier", "extention", 4,
                System.currentTimeMillis(), null, false, null, false, null );
        deployGav( withClassifierAndExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        downloadAndVerify( withClassifierAndExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
    }

    @Test
    public void testNonGavArtifacts()
        throws Exception
    {
        // deploy a non maven path
        new DeployUtils( this ).deployWithWagon( "http", getRepositoryUrl( getTestRepositoryId() ),
            getTestFile( "pom.xml" ), "foo/bar" );

        // now get the info for it
        Response response =
            RequestFacade.doGetRequest( "service/local/repositories/" + getTestRepositoryId() + "/content/"
                + "foo/bar" + "?describe=maven2" );
        String responseText = response.getEntity().getText();
        Assert.assertEquals( response.getStatus().getCode(), 404, "Response was: " + responseText );

    }

    public void deployGav( Gav gav, String repoId )
        throws Exception
    {
        new DeployUtils( this ).deployWithWagon( "http", getRepositoryUrl( repoId ),
            getTestFile( "simpleJar.jar" ), getRelitiveArtifactPath( gav ) );
    }

    @Test
    public void notFoundTest()
        throws Exception
    {
        Gav releaseNotFoundGav =
            new Gav( "nexus3615", "notFound", "1.0.1", null, "jar", null, null, null, false, null, false, null );
        Response response = downloadView( releaseNotFoundGav, "maven2", getTestRepositoryId() );
        Assert.assertEquals( response.getStatus().getCode(), 404, "Status found: " + response.getStatus() );

        Gav snapshotNotFoundGav =
            new Gav( "nexus3615", "notFound", "1.0.1-SNAPSHOT", null, "jar", 1, System.currentTimeMillis(), null, 
                false, null, false, null );
        response = downloadView( snapshotNotFoundGav, "maven2", getTestRepositoryId() );
        Assert.assertEquals( response.getStatus().getCode(), 404, "Status found: " + response.getStatus() );
    }

    private void downloadAndVerify( Gav gav, String repoId )
        throws IOException
    {
        Maven2ArtifactInfoResource data = downloadViewResource( gav, repoId );

        Assert.assertEquals( gav.getArtifactId(), data.getArtifactId() );
        Assert.assertEquals( gav.getGroupId(), data.getGroupId() );

        if ( gav.isSnapshot() && gav.getSnapshotTimeStamp() != null )
        {
            // time stamp snapshot
            String expectedVersion =
                gav.getVersion().replaceFirst(
                    "SNAPSHOT",
                    new SimpleDateFormat( "yyyyMMdd.HHmmss" ).format( new Date( gav.getSnapshotTimeStamp() ) ) + "-"
                        + gav.getSnapshotBuildNumber() );
            Assert.assertEquals( expectedVersion, data.getVersion() );
        }
        else
        {
            // non snapshot
            Assert.assertEquals( gav.getVersion(), data.getVersion() );
        }
        Assert.assertEquals( gav.getBaseVersion(), data.getBaseVersion() );
        Assert.assertEquals( gav.getClassifier(), data.getClassifier() );
        Assert.assertEquals( gav.getExtension(), data.getExtension() );

        Assert.assertEquals( data.getDependencyXmlChunk(), buildExpectedDepBlock( gav ) );
    }

    private Maven2ArtifactInfoResource downloadViewResource( Gav gav, String repoId )
        throws IOException
    {
        XStream xstream = getXMLXStream();

        Response response = downloadView( gav, "maven2", repoId );

        String responseText = response.getEntity().getText();

        Assert.assertEquals( 200, response.getStatus().getCode() );

        Maven2ArtifactInfoResource data =
            ( (Maven2ArtifactInfoResourceRespose) xstream.fromXML( responseText ) ).getData();

        return data;
    }

    private String buildExpectedDepBlock( Gav gav )
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "<dependency>\n" );
        buffer.append( "  <groupId>" ).append( gav.getGroupId() ).append( "</groupId>\n" );
        buffer.append( "  <artifactId>" ).append( gav.getArtifactId() ).append( "</artifactId>\n" );
        buffer.append( "  <version>" ).append( gav.getBaseVersion() ).append( "</version>\n" );

        if ( gav.getClassifier() != null )
        {
            buffer.append( "  <classifier>" ).append( gav.getClassifier() ).append( "</classifier>\n" );
        }

        if ( gav.getExtension() != null && !gav.getExtension().equals( "jar" ) )
        {
            buffer.append( "  <type>" ).append( gav.getExtension() ).append( "</type>\n" );
        }

        buffer.append( "</dependency>" );

        return buffer.toString();
    }

    private Response downloadView( Gav gav, String describeKey, String repoId )
        throws IOException
    {
        Response response =
            RequestFacade.doGetRequest( "service/local/repositories/" + repoId + "/content/"
                + getRelitiveArtifactPath( gav ) + "?describe=" + describeKey );

        return response;
    }
}
