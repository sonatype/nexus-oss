package org.sonatype.nexus.selenium.nexus2195;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.GroupConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;

public class Nexus2195GroupTest
    extends SeleniumTest
{

    @Test
    public void errorMessagesGroup()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        GroupConfigurationForm newGroup = main.openRepositories().addGroup().save();

        Assert.assertTrue( "Task type is a required field",
                           newGroup.getIdField().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Name is a required field", newGroup.getName().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Name is a required field", newGroup.getProvider().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Name is a required field", newGroup.getPublishUrl().hasErrorText( "This field is required" ) );
    }

    @Test
    public void errorMessagesRepositories()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        GroupConfigurationForm newGroup =
            main.openRepositories().addGroup().populate( "seleniumgroupid", "seleniumgroupname", "maven2", false ).save();

        Assert.assertTrue( "Repositories: Select one or more items",
                           newGroup.getRepositories().hasErrorText( "Select one or more items" ) );

        newGroup.getRepositories().add( "thirdparty" );
        newGroup.getRepositories().add( "central" );
        newGroup.getRepositories().add( "releases" );

        Assert.assertFalse( "Error message still there after the problem is fixed",
                           newGroup.getRepositories().hasErrorText( "Select one or more items" ) );

    }

    @Test
    public void selectProvider()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        GroupConfigurationForm newGroup = main.openRepositories().addGroup();

        newGroup.getProvider().select( 0 );
        Assert.assertEquals( "maven1", newGroup.getFormat().getValue() );
    }

    @Test
    public void crudGroup()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String groupId = "selenium-group";
        String name = "Selenium group";
        repositories.addGroup().populate( groupId, name, "maven2", true, "thirdparty", "central" ).save();
        repositories.refresh();

        // read
        GroupConfigurationForm config =
            (GroupConfigurationForm) repositories.select( groupId, RepoKind.GROUP ).selectConfiguration( RepoKind.GROUP );

        Assert.assertEquals( groupId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config =
            (GroupConfigurationForm) repositories.select( groupId, RepoKind.GROUP ).selectConfiguration( RepoKind.GROUP );

        String newName = "new selenium group name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config =
            (GroupConfigurationForm) repositories.select( groupId, RepoKind.GROUP ).selectConfiguration( RepoKind.GROUP );
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( groupId, RepoKind.GROUP );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( groupId ) );
    }

}
