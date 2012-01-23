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
package org.sonatype.nexus.selenium.nexus2145;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositoriesConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2145HostedRepositoryTest.class )
public class Nexus2145HostedRepositoryTest
    extends SeleniumTest
{

    @Test
    public void crudHosted()
        throws InterruptedException
    {
        doLogin();

        MockListener<RepositoryResourceResponse> ml =
            MockHelper.listen( "/repositories", new MockListener<RepositoryResourceResponse>() );
        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-repo";
        String name = "Selenium repository";
        repositories.addHostedRepo().populate( repoId, name ).save();
        ml.waitForResult( RepositoryResourceResponse.class );
        MockHelper.clearMocks();
        repositories.refresh();

        // read
        ml = MockHelper.listen( "/repositories/{repositoryId}", new MockListener<RepositoryResourceResponse>() );
        RepositoriesEditTabs select = repositories.select( repoId, RepoKind.HOSTED );
        ml.waitForResult( RepositoryResourceResponse.class );
        MockHelper.clearMocks();

        RepositoriesConfigurationForm config = (RepositoriesConfigurationForm) select.selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "hosted", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) select.selectConfiguration();

        String newName = "new selenium repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) select.selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.HOSTED );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

}
