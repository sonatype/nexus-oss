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
package org.sonatype.nexus.plugins.migration.nexus2500;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrator;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;

public class Nexus2500CheckMigrationLogCreationIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void checkAvailableLogs()
        throws Exception
    {
        Response response = RequestFacade.sendMessage( "service/local/logs", Method.GET );
        String responseText = response.getEntity().getText();

        Assert.assertEquals( "Status: \n" + responseText, 200, response.getStatus().getCode() );

        LogsListResourceResponse logListResponse =
            (LogsListResourceResponse) this.getXMLXStream().fromXML( responseText );
        List<LogsListResource> logList = logListResponse.getData();
        Assert.assertTrue( "Log List should contain at least 1 log.", logList.size() > 0 );

        List<String> names = new ArrayList<String>();
        for ( Iterator<LogsListResource> iter = logList.iterator(); iter.hasNext(); )
        {
            LogsListResource logResource = iter.next();

            names.add( logResource.getName() );
        }

        Assert.assertTrue( names.contains( ArtifactoryMigrator.MIGRATION_LOG ) );
    }

}
