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
package org.sonatype.nexus.plugins.migration.nexus1450;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1450LoadMappingIT
    extends AbstractMigrationIntegrationTest
{

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        this.copyConfigFile( "mapping.xml", WORK_CONF_DIR );
    }

    @Test
    public void loadMap()
        throws Exception
    {
        URL url =
            new URL( "http://localhost:" + nexusApplicationPort
                + "/artifactory/artifactory-repo/nexus1450/artifact/1.0/artifact-1.0.jar" );

        Status status = RequestFacade.sendMessage( url, Method.GET, null ).getStatus();
        Assert.assertTrue( "Unable to download artifact " + status + " " + url, status.isSuccess() );
    }

}
