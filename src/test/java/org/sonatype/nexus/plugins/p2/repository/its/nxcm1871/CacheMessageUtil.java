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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1871;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.test.utils.TaskScheduleUtil.waitForAllTasksToStop;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class CacheMessageUtil
{

    public static void expireRepositoryCache( final String... repositories )
        throws Exception
    {
        reindex( false, repositories );
    }

    private static void reindex( final boolean group, final String... repositories )
        throws IOException, Exception
    {
        for ( final String repo : repositories )
        {
            // http://localhost:51292/nexus/service/local/data_cache/repo_groups/p2g/content
            String serviceURI;
            if ( group )
            {
                serviceURI = "service/local/data_cache/repo_groups/" + repo + "/content";
            }
            else
            {
                serviceURI = "service/local/data_cache/repositories/" + repo + "/content";
            }

            final Response response = RequestFacade.sendMessage( serviceURI, Method.DELETE );
            final Status status = response.getStatus();
            assertThat( "Fail to update " + repo + " repository index " + status, status.isSuccess(), is( true ) );
        }

        // let w8 a few time for indexes
        waitForAllTasksToStop();
    }

    public static void expireGroupCache( final String... groups )
        throws Exception
    {
        reindex( true, groups );
    }

}
