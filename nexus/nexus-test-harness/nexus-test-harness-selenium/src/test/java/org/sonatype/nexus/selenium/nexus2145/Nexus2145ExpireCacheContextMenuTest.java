package org.sonatype.nexus.selenium.nexus2145;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.testng.annotations.Test;

@Component( role = Nexus2145ExpireCacheContextMenuTest.class )
public class Nexus2145ExpireCacheContextMenuTest
    extends AbstractContextMenuTest
{

    @Test
    public void contextMenuExpireCache()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // expire cache
        MockHelper.expect( EXPIRE_CACHE_URI, new MockResponse( Status.SUCCESS_OK, new NFCResourceResponse() ) );
        repositories.contextMenuExpireCache( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( EXPIRE_CACHE_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuExpireCache( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

}
