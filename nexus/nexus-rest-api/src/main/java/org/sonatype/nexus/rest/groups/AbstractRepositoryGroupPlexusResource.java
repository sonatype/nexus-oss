/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.groups;

import java.util.Collection;

import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;

public abstract class AbstractRepositoryGroupPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";

    public void validateGroup( RepositoryGroupResource resource, Request request )
        throws NoSuchRepositoryException,
            InvalidGroupingException,
            ResourceException
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
