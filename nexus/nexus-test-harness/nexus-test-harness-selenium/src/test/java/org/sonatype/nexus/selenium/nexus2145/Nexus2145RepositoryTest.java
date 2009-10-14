package org.sonatype.nexus.selenium.nexus2145;

import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositoriesConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.testng.Priority;
import org.testng.Assert;
import org.testng.annotations.Test;

@Priority( 10 )
@Component( role = Nexus2145RepositoryTest.class )
public class Nexus2145RepositoryTest
    extends SeleniumTest
{

    @Test
    public void errorMessagesHosted()
        throws InterruptedException
    {
        doLogin();

        RepositoriesConfigurationForm newHosted = main.openRepositories().addHostedRepo().save();

        assertTrue( "Task type is a required field", newHosted.getIdField().hasErrorText( "This field is required" ) );
        assertTrue( "Name is a required field", newHosted.getName().hasErrorText( "This field is required" ) );
    }

    @Test
    public void crudHosted()
        throws InterruptedException
    {
        doLogin();

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-repo";
        String name = "Selenium repository";
        repositories.addHostedRepo().populate( repoId, name ).save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config =
            (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.HOSTED ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "hosted", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.HOSTED ).selectConfiguration();

        String newName = "new selenium repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.HOSTED ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.HOSTED );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

    @Test
    public void crudVirtual()
        throws InterruptedException
    {
        doLogin();

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-virtual-repo";
        String name = "Selenium Virtual repository";
        RepositoriesConfigurationForm virtualRepo =
            repositories.addVirtualRepo().populateVirtual( repoId, name, "m2-m1-shadow", "releases" );
        virtualRepo.save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config =
            (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.VIRTUAL ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "virtual", config.getType().getValue() );
        Assert.assertEquals( "maven1", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.VIRTUAL ).selectConfiguration();

        String newName = "new selenium virtual repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.VIRTUAL ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.VIRTUAL );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

    @Test
    public void crudProxy()
        throws InterruptedException
    {
        doLogin();

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-proxy-repo";
        String name = "Selenium Proxy repository";
        repositories.addProxyRepo().populateProxy( repoId, name,
                                                   "http://repository.sonatype.org/content/groups/public/" ).save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config =
            (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.PROXY ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "proxy", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.PROXY ).selectConfiguration();

        String newName = "new selenium proxy repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.PROXY ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.PROXY );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

}
