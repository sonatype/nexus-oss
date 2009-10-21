package org.sonatype.nexus.selenium.nexus2145;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.testng.annotations.Test;

@Component( role = Nexus2145ReindexContextMenuTest.class )
public class Nexus2145ReindexContextMenuTest
    extends AbstractContextMenuTest
{

    @Test
    public void contextMenuIndex()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // reindex
        MockHelper.expect( REINDEX_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuReindex( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( REINDEX_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuReindex( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        // incremental reindex
        MockHelper.expect( INCREMENTAL_REINDEX_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuIncrementalReindex( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( INCREMENTAL_REINDEX_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuIncrementalReindex( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

}
