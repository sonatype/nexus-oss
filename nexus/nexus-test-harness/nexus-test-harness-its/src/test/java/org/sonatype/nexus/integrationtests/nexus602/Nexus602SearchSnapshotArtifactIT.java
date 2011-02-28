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
package org.sonatype.nexus.integrationtests.nexus602;

import java.net.URL;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test snapshot search results can be downloaded.
 */
public class Nexus602SearchSnapshotArtifactIT
    extends AbstractNexusIntegrationTest
{

    private Gav gav;

    public Nexus602SearchSnapshotArtifactIT()
        throws Exception
    {
        gav =
            new Gav( "nexus602", "artifact", "1.0-SNAPSHOT", null, "jar", 0, 0L, null, false, null, false, null );
    }

    @Test
    public void searchSnapshot()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPO_TEST_HARNESS_SNAPSHOT_REPO + "&g=" + gav.getGroupId()
                + "&a=" + gav.getArtifactId() + "&v=" + gav.getVersion();
        Response response = RequestFacade.doGetRequest( serviceURI );
        Assert.assertEquals( response.getStatus().getCode(), 301, "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString() );

        Reference redirectRef = response.getRedirectRef();
        Assert.assertNotNull( redirectRef, "Snapshot download should redirect to a new file "
            + response.getRequest().getResourceRef().toString() );

        serviceURI = redirectRef.toString();

        response = RequestFacade.sendMessage( new URL( serviceURI ), Method.GET, null );

        Assert.assertTrue( response.getStatus().isSuccess(), "Unable to fetch snapshot artifact" );
    }

    @Test
    public void searchRelease()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPO_TEST_HARNESS_REPO + "&g=" + getTestId() + "&a="
                + "artifact" + "&v=" + "1.0";
        Response response = RequestFacade.doGetRequest( serviceURI );

        Assert.assertEquals( response.getStatus().getCode(), 301, "Should redirect to a new file "
            + response.getRequest().getResourceRef().toString() );

        Reference redirectRef = response.getRedirectRef();
        Assert.assertNotNull( redirectRef, "Should redirect to a new file "
            + response.getRequest().getResourceRef().toString() );

        serviceURI = redirectRef.toString();

        response = RequestFacade.sendMessage( new URL( serviceURI ), Method.GET, null );

        Assert.assertTrue( response.getStatus().isSuccess(), "fetch released artifact" );
    }

    @Test
    public void searchInvalidArtifact()
        throws Exception
    {
        String serviceURI =
            "service/local/artifact/maven/redirect?r=" + REPO_TEST_HARNESS_REPO + "&g=" + "invalidGroupId" + "&a="
                + "invalidArtifact" + "&v=" + "32.64";
        Response response = RequestFacade.doGetRequest( serviceURI );

        Assert.assertEquals( response.getStatus().getCode(), 404, "Shouldn't find an invalid artifact" );
    }

}
