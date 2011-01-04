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
package org.sonatype.nexus.integrationtests.nexus1560;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus1560LegacyAllowRulesIT
    extends AbstractLegacyRulesIT
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @BeforeMethod
    public void init()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "read" );

        // Now need the view priv as well
        addPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
    }

    @Test
    public void fromRepository()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gavArtifact1 );

        Status status = download( downloadUrl ).getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to download artifact from repository " + status );
    }

    @Test
    public void fromGroup()
        throws Exception
    {
        String downloadUrl =
            GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact1 );

        Status status = download( downloadUrl ).getStatus();
        Assert.assertEquals( status.getCode(), 403, "Unable to download artifact from repository: " + status );
    }

    @Test( expectedExceptions = FileNotFoundException.class )
    public void checkMetadataOnGroup()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        downloadFile( new URL( nexusBaseUrl + GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP
            + "/nexus1560/artifact/maven-metadata.xml" ), "./target/downloads/nexus1560/repo-maven-metadata.xml" );
    }

    @Test
    public void checkMetadataOnRepository()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        File file =
            downloadFile( new URL( nexusBaseUrl + REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO
                + "/nexus1560/artifact/maven-metadata.xml" ), "./target/downloads/nexus1560/repo-maven-metadata.xml" );
        Xpp3Dom dom = Xpp3DomBuilder.build( new FileReader( file ) );
        Xpp3Dom[] versions = dom.getChild( "versioning" ).getChild( "versions" ).getChildren( "version" );
        for ( Xpp3Dom version : versions )
        {
            Assert.assertEquals( version.getValue(), "1.0", "Invalid version available on metadata" + dom.toString() );
        }
    }

    @Test
    public void artifact2FromGroup()
        throws Exception
    {
        String downloadUrl =
            GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact2 );

        failDownload( downloadUrl );
    }

    @Test
    public void artifact2FromRepo()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gavArtifact2 );

        failDownload( downloadUrl );
    }

    @Test
    public void artifact2FromRepo2()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO2 + "/" + getRelitiveArtifactPath( gavArtifact2 );

        failDownload( downloadUrl );
    }

}
