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
package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.callback.EntityRequestCallback;
import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.PathUtils;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.services.RepositoriesService;
import org.sonatype.nexus.gwt.client.services.RepositoryService;

public class DefaultRepositoriesService
    extends AbstractNexusService
    implements RepositoriesService
{

    public DefaultRepositoriesService( Nexus nexus, String path )
    {
        super( nexus, path );
    }

    public void getRepositories( EntityResponseHandler handler )
    {
        get( new EntityRequestCallback( handler ), getNexus().getDefaultVariant() );
    }

    public RepositoryService getRepositoryById( String id )
    {
        return getRepositoryByPath( PathUtils.append( getPath(), id ) );
    }

    public RepositoryService getRepositoryByPath( String path )
    {
        return new DefaultRepositoryService( getNexus(), path );
    }

    public RepositoryService createRepository( String id, Representation representation, StatusResponseHandler handler )
    {
        RepositoryService repository = getRepositoryById( id );

        repository.create( representation, handler );

        return repository;
    }

}
