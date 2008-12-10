/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus602;

import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

/**
 * Test snapshot search results can be downloaded.
 */
public class Nexus602SearchSnapshotArtifactTest
    extends AbstractNexusIntegrationTest
{

    private static final Gav SNAPSHOT_ARTIFACT =
        new Gav( "nexus602", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, false, false, null, false, null );

    @Test
    public void searchSnapshot()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPO_TEST_HARNESS_SNAPSHOT_REPO + "&g="
                + SNAPSHOT_ARTIFACT.getGroupId() + "&a=" + SNAPSHOT_ARTIFACT.getArtifactId() + "&v="
                + SNAPSHOT_ARTIFACT.getVersion();
        Response response = RequestFacade.doGetRequest( serviceURI );
        Assert.assertEquals( "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString(), 301, response.getStatus().getCode() );

        Reference redirectRef = response.getRedirectRef();
        Assert.assertNotNull( "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString(), redirectRef );

        serviceURI = redirectRef.toString();

        response = RequestFacade.sendMessage( new URL( serviceURI ), Method.GET, null );

        Assert.assertTrue( "Unable to fetch snapshot artifact", response.getStatus().isSuccess() );
    }

    @Test
    public void searchRelease()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPO_TEST_HARNESS_REPO + "&g=" + getTestId()
                + "&a=" + "artifact" + "&v=" + "1.0";
        Response response = RequestFacade.doGetRequest( serviceURI );

        Assert.assertEquals( "Should redirect to a new file " + response.getRequest().getResourceRef().toString(), 301,
                             response.getStatus().getCode() );

        Reference redirectRef = response.getRedirectRef();
        Assert.assertNotNull( "Should redirect to a new file " + response.getRequest().getResourceRef().toString(),
                              redirectRef );

        serviceURI = redirectRef.toString();

        response = RequestFacade.sendMessage( new URL( serviceURI ), Method.GET, null );

        Assert.assertTrue( "fetch released artifact", response.getStatus().isSuccess() );
    }

    @Test
    public void searchInvalidArtifact()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPO_TEST_HARNESS_REPO + "&g=" + "invalidGroupId"
                + "&a=" + "invalidArtifact" + "&v=" + "32.64";
        Response response = RequestFacade.doGetRequest( serviceURI );

        Assert.assertEquals( "Shouldn't find an invalid artifact", 404, response.getStatus().getCode() );
    }

}
