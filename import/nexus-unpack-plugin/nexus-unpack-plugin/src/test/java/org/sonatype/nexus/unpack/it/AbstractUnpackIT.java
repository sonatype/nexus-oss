/*
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
package org.sonatype.nexus.unpack.it;

import java.io.IOException;

import org.junit.Before;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public abstract class AbstractUnpackIT
    extends AbstractNexusIntegrationTest
{
    @Override
    @Before
    public void oncePerClassSetUp()
        throws Exception
    {
        super.oncePerClassSetUp();
        setIndexingEnabled( false );
    }

    protected void setIndexingEnabled( final boolean enabled )
        throws IOException, InterruptedException
    {
        // remember secure value
        final boolean prevSecureTest = TestContainer.getInstance().getTestContext().isSecureTest();
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        // disable indexing for repository we use for testing.
        // Indexer PROBABLY causes issues like https://issues.sonatype.org/browse/NXCM-3986
        final RepositoryMessageUtil rmu = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        // we know from test premises what we will get back (not a group, not a shadow)
        final RepositoryResource repoModel = (RepositoryResource) rmu.getRepository( REPO_TEST_HARNESS_REPO );
        repoModel.setIndexable( enabled );
        rmu.updateRepo( repoModel );
        // set back what it was
        TestContainer.getInstance().getTestContext().setSecureTest( prevSecureTest );
        getEventInspectorsUtil().waitForCalmPeriod();
    }
}
