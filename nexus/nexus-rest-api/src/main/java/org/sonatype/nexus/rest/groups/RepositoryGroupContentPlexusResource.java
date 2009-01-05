/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.groups;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryGroupContentPlexusResource" )
public class RepositoryGroupContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";

    @Override
    public Object getPayloadInstance()
    {
        // group content is read only
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_groups/{" + GROUP_ID_KEY + "}/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*/content/**", "authcBasic,tgperms" );
    }

    @Override
    protected ResourceStore getResourceStore( Request request )
        throws NoSuchRepositoryException,
            ResourceException
    {
        String groupId = request.getAttributes().get( GROUP_ID_KEY ).toString();

        return getNexus().getRepositoryWithFacet( groupId, GroupRepository.class );
    }

}
