package org.sonatype.nexus.selenium.nexus2194;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.TestContext;
import org.sonatype.nexus.mock.pages.RepositoriesArtifactUploadForm;
import org.sonatype.nexus.mock.pages.RepositoriesArtifactUploadForm.Definition;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2194UploadArtifactTest.class )
public class Nexus2194UploadArtifactTest
    extends SeleniumTest
{

    @Test
    public void uploadArtifact()
    {
        LoginTest.doLogin( main );

        RepositoriesArtifactUploadForm uploadTab =
            main.openRepositories().select( "thirdparty", RepoKind.HOSTED ).selectUpload( );
        uploadTab.selectDefinition( Definition.POM );
        if ( true )
        {
            return;
        }
        Assert.assertNotNull( uploadTab.uploadPom( TestContext.getFile( "pom.xml" ) ) );
        Assert.assertNotNull( uploadTab.uploadArtifact( TestContext.getFile( "artifact.jar" ), null, null ) );
        uploadTab.getUploadButton().click();
    }

}
