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
package org.sonatype.nexus.rest.contentclasses;

import java.util.Collection;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

/**
 * The ContentClasses list resource. This handles the GET method only and simply returns the list of existing nexus
 * ContentClasses.
 * 
 * @author cstamas
 * @author tstevens
 * @plexus.component role-hint="ContentClassesListPlexusResource"
 */
public class ContentClassesListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_content_classes";
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor(
            "/service/*/repo_content_classes",
            "authcBasic,perms[nexus:repocontentclasses]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Collection<ContentClass> contentClasses = getNexusInstance( request ).listRepositoryContentClasses();

        RepositoryContentClassListResourceResponse result = new RepositoryContentClassListResourceResponse();

        for ( ContentClass contentClass : contentClasses )
        {
            RepositoryContentClassListResource resource = new RepositoryContentClassListResource();

            resource.setContentClass( contentClass.getId() );

            // trickery, let's get the Class name for ContentClass name
            String name = contentClass.getClass().getName();

            if ( name.contains( "." ) )
            {
                name = name.substring( name.lastIndexOf( "." ) + 1, name.length() );
            }

            resource.setName( name );

            result.addData( resource );
        }

        return result;
    }

}
