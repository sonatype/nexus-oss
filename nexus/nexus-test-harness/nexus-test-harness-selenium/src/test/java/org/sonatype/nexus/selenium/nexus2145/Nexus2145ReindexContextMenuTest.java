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

    @Test( enabled = false )
    // TODO can't test plugins
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
