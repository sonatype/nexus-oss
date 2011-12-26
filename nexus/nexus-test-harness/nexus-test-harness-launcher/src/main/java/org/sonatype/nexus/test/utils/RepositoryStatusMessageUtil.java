/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;

public class RepositoryStatusMessageUtil
{

    private static final RepositoriesNexusRestClient REPOSITORY_NRC = new RepositoriesNexusRestClient(
        RequestFacade.getNexusRestClient(),
        new TasksNexusRestClient( RequestFacade.getNexusRestClient() ),
        new EventInspectorsUtil( RequestFacade.getNexusRestClient() )
    );

    public static Response putOutOfService( String repoId, String repoType )
        throws IOException
    {
        return REPOSITORY_NRC.putOutOfService( repoId, repoType );
    }

    public static Response putInService( String repoId, String repoType )
        throws IOException
    {
        return REPOSITORY_NRC.putInService( repoId, repoType );
    }

    /**
     * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
     */
    public static Response changeStatus( RepositoryStatusResource status )
        throws IOException
    {
        return REPOSITORY_NRC.changeStatus( status );
    }

}
