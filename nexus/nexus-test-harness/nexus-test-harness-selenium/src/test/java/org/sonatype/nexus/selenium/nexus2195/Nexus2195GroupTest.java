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
