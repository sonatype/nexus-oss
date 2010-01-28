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
