package org.sonatype.nexus.mock;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.pages.RepositoriesConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesTab;

public class RepositoryTest
    extends SeleniumTest
{

    @Test
    public void errorMessagesHosted()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RepositoriesConfigurationForm newHosted = main.openRepositories().addHostedRepo().save();

        Assert.assertTrue( "Task type is a required field",
                           newHosted.getIdField().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Name is a required field", newHosted.getName().hasErrorText( "This field is required" ) );
    }

    @Test
    public void crudHosted()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-repo";
        String name = "Selenium repository";
        repositories.addHostedRepo().populate( repoId, name ).save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config = repositories.select( repoId ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "hosted", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = repositories.select( repoId ).selectConfiguration();

        String newName = "new selenium repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = repositories.select( repoId ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

}
