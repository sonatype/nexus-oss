/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.repotargets;

import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;

public class AbstractRepositoryTargetResourceHandler
    extends AbstractNexusResourceHandler
{
    public static final String REPO_TARGET_ID_KEY = "repoTargetId";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractRepositoryTargetResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected RepositoryTargetResource getNexusToRestResource( CRepositoryTarget target )
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        resource.setId( target.getId() );

        resource.setName( target.getName() );

        resource.setResourceURI( getRequest().getResourceRef().getPath() );

        resource.setContentClass( target.getContentClass() );

        List<String> patterns = target.getPatterns();

        for ( String pattern : patterns )
        {
            resource.addPattern( pattern );
        }

        return resource;
    }

    protected CRepositoryTarget getRestToNexusResource( RepositoryTargetResource resource )
    {
        CRepositoryTarget target = new CRepositoryTarget();

        target.setId( resource.getId() );

        target.setName( resource.getName() );

        target.setContentClass( resource.getContentClass() );

        List<String> patterns = resource.getPatterns();

        for ( String pattern : patterns )
        {
            target.addPattern( pattern );
        }

        return target;
    }

    protected boolean validate( boolean isNew, RepositoryTargetResource resource, Representation representation )
    {
        if ( isNew )
        {
            if ( resource.getId() == null )
            {
                resource.setId( Long.toHexString( System.currentTimeMillis() ) );
            }
        }

        if ( resource.getId() == null )
        {
            return false;
        }

        return true;
    }

}
