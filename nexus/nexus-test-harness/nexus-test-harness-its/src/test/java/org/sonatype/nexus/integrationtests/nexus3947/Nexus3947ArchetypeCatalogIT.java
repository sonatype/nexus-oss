/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus3947;

import java.net.URL;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3947ArchetypeCatalogIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testArchetypeCatalog()
        throws Exception
    {
        Response response;

        ArchetypeCatalog catalog;

        ArchetypeCatalogXpp3Reader acr = new ArchetypeCatalogXpp3Reader();

        // path of catalog
        String relativePath = "archetype-catalog.xml";
        String url = getRepositoryUrl( getTestRepositoryId() ) + relativePath;

        // request the catalog
        response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        // read and check
        catalog = acr.read( response.getEntity().getReader() );
        Assert.assertEquals( catalog.getArchetypes().size(), 1 );

        // deploy one new archetype
        int httpResponseCode =
            getDeployUtils().deployUsingPomWithRest( getTestRepositoryId(), getTestFile( "simple-archetype2.jar" ),
                getTestFile( "simple-archetype2.pom" ), null, null );
        Assert.assertTrue( Status.isSuccess( httpResponseCode ), "Unable to deploy artifact " + httpResponseCode );
        
        // wait
        getEventInspectorsUtil().waitForCalmPeriod();

        // request the catalog
        response = RequestFacade.sendMessage( new URL( url ), Method.GET, null );

        // read and check
        catalog = acr.read( response.getEntity().getReader() );
        Assert.assertEquals( catalog.getArchetypes().size(), 2 );
    }
}
