/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.repotargets;

import java.util.List;

import org.restlet.data.Request;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;

public abstract class AbstractRepositoryTargetPlexusResource
    extends AbstractNexusPlexusResource
{

    @SuppressWarnings( "unchecked" )
    protected RepositoryTargetResource getNexusToRestResource( CRepositoryTarget target, Request request )
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        resource.setId( target.getId() );

        resource.setName( target.getName() );

        resource.setResourceURI( request.getResourceRef().getPath() );

        resource.setContentClass( target.getContentClass() );

        List<String> patterns = target.getPatterns();

        for ( String pattern : patterns )
        {
            resource.addPattern( pattern );
        }

        return resource;
    }

    @SuppressWarnings( "unchecked" )
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

    protected boolean validate( boolean isNew, RepositoryTargetResource resource )
    {
        if ( isNew )
        {
            if ( resource.getId() == null )
            {
                resource.setId( Long.toHexString( System.nanoTime() ) );
            }
        }

        if ( resource.getId() == null )
        {
            return false;
        }

        return true;
    }

}
