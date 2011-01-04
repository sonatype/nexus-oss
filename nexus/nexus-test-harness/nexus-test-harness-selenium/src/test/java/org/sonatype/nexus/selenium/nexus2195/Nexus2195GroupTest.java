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
package org.sonatype.nexus.selenium.nexus2195;

import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.GroupConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2195GroupTest.class )
public class Nexus2195GroupTest
    extends SeleniumTest
{

    @Test
    public void errorMessagesGroup()
        throws InterruptedException
    {
        doLogin();

        GroupConfigurationForm newGroup = main.openRepositories().addGroup().save();

        assertTrue( "Task type is a required field", newGroup.getIdField().hasErrorText( "This field is required" ) );
        assertTrue( "Name is a required field", newGroup.getName().hasErrorText( "This field is required" ) );
        assertTrue( "Name is a required field", newGroup.getProvider().hasErrorText( "This field is required" ) );
        assertTrue( "Name is a required field", newGroup.getPublishUrl().hasErrorText( "This field is required" ) );
    }

    @Test
    public void selectProvider()
        throws InterruptedException
    {
        doLogin();

        GroupConfigurationForm newGroup = main.openRepositories().addGroup();

        newGroup.getProvider().select( 0 );
        Assert.assertEquals( "maven1", newGroup.getFormat().getValue() );
    }

    @Test
    public void crudGroup()
        throws InterruptedException
    {
        doLogin();

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String groupId = "selenium-group";
        String name = "Selenium group";
        repositories.addGroup().populate( groupId, name, "maven2", true, "thirdparty", "central" ).save();
        repositories.refresh();

        // read
        GroupConfigurationForm config =
            (GroupConfigurationForm) repositories.select( groupId, RepoKind.GROUP ).selectConfiguration();

        Assert.assertEquals( groupId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (GroupConfigurationForm) repositories.select( groupId, RepoKind.GROUP ).selectConfiguration();

        String newName = "new selenium group name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (GroupConfigurationForm) repositories.select( groupId, RepoKind.GROUP ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( groupId, RepoKind.GROUP );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( groupId ) );
    }

}
