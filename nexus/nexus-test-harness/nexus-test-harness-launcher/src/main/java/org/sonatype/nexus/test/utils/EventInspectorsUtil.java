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
package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class EventInspectorsUtil
    extends ITUtil
{
    public EventInspectorsUtil( AbstractNexusIntegrationTest test )
    {
        super( test );
    }

    protected boolean isCalmPeriod()
        throws IOException
    {
        final Response response = RequestFacade.doGetRequest( "service/local/eventInspectors/isCalmPeriod" );

        if ( response.getStatus().isSuccess() )
        {
            // only 200 Ok means calm period,
            // otherwise 202 Accepted is returned
            return response.getStatus().getCode() == Status.SUCCESS_OK.getCode();
        }
        else
        {
            throw new IOException( "The isCalmPeriod REST resource reported an error ("
                + response.getStatus().toString() + "), bailing out!" );
        }
    }

    public void waitForCalmPeriod()
        throws IOException, InterruptedException
    {
        final Response response =
            RequestFacade.doGetRequest( "service/local/eventInspectors/isCalmPeriod?waitForCalm=true" );

        if ( response.getStatus().getCode() != Status.SUCCESS_OK.getCode() )
        {
            throw new IOException( "The isCalmPeriod REST resource reported an error ("
                + response.getStatus().toString() + "), bailing out!" );
        }
    }
}
