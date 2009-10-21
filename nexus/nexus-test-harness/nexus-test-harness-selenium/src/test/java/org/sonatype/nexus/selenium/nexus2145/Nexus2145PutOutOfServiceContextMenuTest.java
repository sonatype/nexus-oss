package org.sonatype.nexus.selenium.nexus2145;

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
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.testng.annotations.Test;

@Component( role = Nexus2145PutOutOfServiceContextMenuTest.class )
public class Nexus2145PutOutOfServiceContextMenuTest
    extends AbstractContextMenuTest
{

    @Test
    public void contextMenuOutofService()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // put out of service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuPutOutOfService( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuPutOutOfService( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( hostedRepo.getLocalStatus(), equalTo( LocalStatus.OUT_OF_SERVICE ) );
        // check on UI
        assertThat( repositories.getStatus( hostedRepo.getId() ), StringStartsWith.startsWith( "Out of Service" ) );

        // back to service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST,
                                                                                    null ) );
        repositories.contextMenuPutInService( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuPutInService( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( hostedRepo.getLocalStatus(), equalTo( LocalStatus.IN_SERVICE ) );
        // check on UI
        assertThat( repositories.getStatus( hostedRepo.getId() ), StringStartsWith.startsWith( "In Service" ) );
    }

}
