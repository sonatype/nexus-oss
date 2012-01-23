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
