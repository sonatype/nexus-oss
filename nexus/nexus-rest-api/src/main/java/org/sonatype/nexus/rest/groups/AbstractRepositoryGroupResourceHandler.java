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
package org.sonatype.nexus.rest.groups;

import java.util.Collection;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;

public abstract class AbstractRepositoryGroupResourceHandler
    extends AbstractNexusResourceHandler
{
    public static final String GROUP_ID_KEY = "groupId";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractRepositoryGroupResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public void validateGroup( RepositoryGroupResource resource )
        throws NoSuchRepositoryException,
            InvalidGroupingException
    {
        ContentClass cc = null;

        for ( RepositoryGroupMemberRepository member : (Collection<RepositoryGroupMemberRepository>) resource
            .getRepositories() )
        {
            Repository repo = getNexus().getRepository( member.getId() );

            if ( cc == null )
            {
                cc = repo.getRepositoryContentClass();
            }
            else
            {
                if ( !cc.isCompatible( repo.getRepositoryContentClass() ) )
                {
                    throw new InvalidGroupingException( cc, repo.getRepositoryContentClass() );
                }
            }

        }
    }

}
