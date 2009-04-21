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
package org.sonatype.nexus.rest.privileges;

import java.util.Collection;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.model.PrivilegeResource;
import org.sonatype.security.rest.model.PrivilegeResourceRequest;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

/**
 * Handles the GET and POST request for the Nexus privileges.
 * 
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "PrivilegeListPlexusResource" )
public class PrivilegeListPlexusResource
    extends AbstractPrivilegePlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return new PrivilegeResourceRequest();
    }

    @Override
    public String getResourceUri()
    {
        return "/privileges";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[security:privileges]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        PrivilegeListResourceResponse result = new PrivilegeListResourceResponse();

        Collection<SecurityPrivilege> privs = getPlexusSecurity().listPrivileges();

        for ( SecurityPrivilege priv : privs )
        {
            PrivilegeStatusResource res = nexusToRestModel( priv, request );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }
}
