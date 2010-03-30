package org.sonatype.nexus.selenium.nexus2145;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.hamcrest.text.StringStartsWith;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.testng.annotations.Test;

@Component( role = Nexus2145BlockProxyContextMenuTest.class )
public class Nexus2145BlockProxyContextMenuTest
    extends AbstractContextMenuTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void contextMenuBlockProxy()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // block proxy
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuBlockProxy( proxyRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuBlockProxy( proxyRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( proxyRepo.getProxyMode(), equalTo( ProxyMode.BLOCKED_MANUAL ) );
        // check on UI
        assertThat( repositories.getStatus( proxyRepo.getId() ),
                    anyOf( equalTo( "In Service - Remote Manually Blocked and Available" ),
                           equalTo( "In Service - Remote Manually Blocked and Unavailable" ) ) );

        // allow proxy
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuAllowProxy( proxyRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuAllowProxy( proxyRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( proxyRepo.getProxyMode(), equalTo( ProxyMode.BLOCKED_AUTO ) );
        // check on UI
        assertThat( repositories.getStatus( proxyRepo.getId() ), StringStartsWith.startsWith( "In Service" ) );
    }

}
