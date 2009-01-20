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
package org.sonatype.nexus.rest.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Resource handler for Repository content resource.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "RepositoryContentPlexusResource" )
public class RepositoryContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
{

    public RepositoryContentPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories/{" + AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY + "}/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/content/**", "authcBasic,trperms" );
    }

    public boolean acceptsUpload()
    {
        return true;
    }

    @Override
    protected ResourceStore getResourceStore( Request request )
        throws NoSuchRepositoryException,
            ResourceException
    {
        return getNexus().getRepository(
            request.getAttributes().get( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ).toString() );
    }

}
