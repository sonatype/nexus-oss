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

@Component( role = Nexus2145RebuildMetadataContextMenuTest.class )
public class Nexus2145RebuildMetadataContextMenuTest
    extends AbstractContextMenuTest
{

    @Test
    public void contextMenuRebuildMetadata()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // rebuild metadata
        MockHelper.expect( REBUILD_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuRebuildMetadata( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( REBUILD_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuRebuildMetadata( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

}
