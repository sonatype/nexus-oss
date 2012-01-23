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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import org.codehaus.plexus.component.annotations.Component;
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
        assertThat(
            repositories.getStatus( proxyRepo.getId() ),
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
        assertThat( repositories.getStatus( proxyRepo.getId() ), startsWith( "In Service" ) );
    }

}
