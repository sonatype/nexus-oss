package org.sonatype.nexus.integrationtests.nexus1560;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;

public class Nexus1560LegacyAllowRulesIT
    extends AbstractLegacyRulesIT
{

    @Before
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
        Assert.assertTrue( "Unable to download artifact from repository " + status, status.isSuccess() );
    }

    @Test
    public void fromGroup()
        throws Exception
    {
        String downloadUrl =
            GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact1 );

        Status status = download( downloadUrl ).getStatus();
        Assert.assertEquals( "Unable to download artifact from repository: " + status, 403, status.getCode() );
    }

    @Test( expected = FileNotFoundException.class )
    public void checkMetadataOnGroup()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        downloadFile( new URL( baseNexusUrl + GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP
            + "/nexus1560/artifact/maven-metadata.xml" ), "./target/downloads/nexus1560/repo-maven-metadata.xml" );
    }

    @Test
    public void checkMetadataOnRepository()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        File file =
            downloadFile( new URL( baseNexusUrl + REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO
                + "/nexus1560/artifact/maven-metadata.xml" ), "./target/downloads/nexus1560/repo-maven-metadata.xml" );
        Xpp3Dom dom = Xpp3DomBuilder.build( new FileReader( file ) );
        Xpp3Dom[] versions = dom.getChild( "versioning" ).getChild( "versions" ).getChildren( "version" );
        for ( Xpp3Dom version : versions )
        {
            Assert.assertEquals( "Invalid version available on metadata" + dom.toString(), "1.0", version.getValue() );
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
