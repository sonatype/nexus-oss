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
package org.sonatype.nexus.selenium.nexus2194;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositoriesArtifactUploadForm;
import org.sonatype.nexus.mock.pages.RepositoriesArtifactUploadForm.Definition;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2194UploadArtifactTest.class )
public class Nexus2194UploadArtifactTest
    extends SeleniumTest
{

    @Test
    public void uploadArtifact()
    {
        doLogin();

        RepositoriesArtifactUploadForm uploadTab =
            main.openRepositories().select( "thirdparty", RepoKind.HOSTED ).selectUpload();
        uploadTab.selectDefinition( Definition.POM );
        if ( true )
        {
            return;
        }
        Assert.assertNotNull( uploadTab.uploadPom( getFile( "pom.xml" ) ) );
        Assert.assertNotNull( uploadTab.uploadArtifact( getFile( "artifact.jar" ), null, null ) );
        uploadTab.getUploadButton().click();
    }

}
