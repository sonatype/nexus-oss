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

@Component( role = Nexus2145ProxyRepositoryTest.class )
public class Nexus2145ProxyRepositoryTest
    extends SeleniumTest
{

    @Test
    public void crudProxy()
        throws InterruptedException
    {
        doLogin();

        MockListener<RepositoryResourceResponse> ml =
            MockHelper.listen( "/repositories", new MockListener<RepositoryResourceResponse>() );
        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-proxy-repo";
        String name = "Selenium proxy repository";
        repositories.addProxyRepo().populateProxy( repoId, name,
        "http://repository.sonatype.org/content/groups/public/" ).save();
        ml.waitForResult( RepositoryResourceResponse.class );
        MockHelper.clearMocks();
        repositories.refresh();

        // read
        ml = MockHelper.listen( "/repositories/{repositoryId}", new MockListener<RepositoryResourceResponse>() );
        RepositoriesEditTabs select = repositories.select( repoId, RepoKind.PROXY );
        ml.waitForResult( RepositoryResourceResponse.class );
        MockHelper.clearMocks();

        RepositoriesConfigurationForm config = (RepositoriesConfigurationForm) select.selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "proxy", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) select.selectConfiguration();

        String newName = "new selenium proxy repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) select.selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.PROXY );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

}
