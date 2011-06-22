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
package org.sonatype.nexus.integrationtests.nexus4390;

import java.io.IOException;
import java.util.Date;

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test tests NEXUS-4390 and Nexus' capability to properly respond to repeated conditionalGET requests. We simply
 * request for a resource, "remember" it's timestamp, and repeatedly ask newer version for the same resource using it's
 * timestamp. In related issue, it is seen that in this case, Nexus eventually responds with 404 because of bug handling
 * the conditional GET.
 * 
 * @author cstamas
 */
public class Nexus4390RepeatedConditionalGetsHaveToSucceedIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testRepeatedConditionalGets()
        throws IOException
    {
        // we can use any resource, since bug stands for all content served by Nexus
        // here, for simplicity, we will use the archetype-catalog.xml which is empty, but that does not
        // matter, since even the empty catalog will respond with 404 when asked for it with NEXUS-4390 being true.
        final String servicePath = "content/repositories/fake-central/archetype-catalog.xml";

        // 1st, we do an unconditional GET to get it's timestamp. Unconditional GET is not affected by
        // this bug, it will succeed.
        final Response response = RequestFacade.sendMessage( servicePath, Method.GET );
        Assert.assertTrue( response.getStatus().isSuccess() );
        final Date lastModified = response.getEntity().getModificationDate();
        response.release();

        // now, we construct and repeat conditional gets
        final String fullUrl = RequestFacade.toNexusURL( servicePath ).toString();
        for ( int i = 0; i < 10; i++ )
        {
            final Request req = new Request( Method.GET, fullUrl );
            req.getConditions().setModifiedSince( lastModified );
            final Response res = RequestFacade.sendMessage( req, null );
            // we are fine with 200 OK, 303 Not Modified or whatever, but not with any server side error or 404
            Assert.assertTrue( res.getStatus().getCode() != 404 && !res.getStatus().isError() );
            res.release();
        }

        // good, we are here, we are fine
    }
}
