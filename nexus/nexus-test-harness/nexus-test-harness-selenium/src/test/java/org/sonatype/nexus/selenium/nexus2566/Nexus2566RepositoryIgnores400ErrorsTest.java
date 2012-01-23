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
package org.sonatype.nexus.selenium.nexus2566;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2566RepositoryIgnores400ErrorsTest.class )
public class Nexus2566RepositoryIgnores400ErrorsTest
    extends SeleniumTest
{

    @Test
    public void createRepo()
        throws InterruptedException
    {
        doLogin();

        MockHelper.expect( "/repositories", new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null, Method.POST ) );

        RepositoriesTab repositories = main.openRepositories();
        String repoId = "nexus2566-repo";
        String name = "nexus2566 repository";
        repositories.addHostedRepo().populate( repoId, name ).save();
        MessageBox mb = new MessageBox( selenium );
        mb.waitForVisible();
        MockHelper.checkAndClean();
        mb.clickOk();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

}
