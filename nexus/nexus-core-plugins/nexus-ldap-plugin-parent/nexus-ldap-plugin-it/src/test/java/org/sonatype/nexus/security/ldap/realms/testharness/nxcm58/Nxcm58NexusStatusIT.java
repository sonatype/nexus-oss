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
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm58;

import java.io.IOException;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.testng.Assert;
import org.testng.annotations.Test;


public class Nxcm58NexusStatusIT
    extends AbstractLdapIntegrationIT
{

    @Test
    public void getStatus()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/status" );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to get nexus status" + status );
    }

    @Test
    public void getLdapInfo()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/ldap/conn_info" );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to reach ldap services\n" + status + "\n"
                + response.getEntity().getText() );
    }

}
