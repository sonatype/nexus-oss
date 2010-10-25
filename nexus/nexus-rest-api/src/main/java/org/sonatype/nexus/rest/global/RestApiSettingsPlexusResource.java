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
package org.sonatype.nexus.rest.global;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.RestApiResourceResponse;
import org.sonatype.nexus.rest.model.RestApiSettings;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * The Smtp settings validation resource.
 * 
 * @author velo
 */
@Component( role = PlexusResource.class, hint = "RestTimeoutSettingsPlexusResource" )
@Path( RestApiSettingsPlexusResource.RESOURCE_URI )
@Consumes( { "application/xml", "application/json" } )
public class RestApiSettingsPlexusResource
    extends AbstractGlobalConfigurationPlexusResource
{
    public static final String RESOURCE_URI = "/rest_api_settings";

    public RestApiSettingsPlexusResource()
    {
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // everybody needs to know the UI timeout
        return null;
    }

    /**
     * Validate smtp settings, send a test email using the configuration.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RestApiSettings.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RestApiResourceResponse resp = new RestApiResourceResponse();
        resp.setData( convert( getGlobalRestApiSettings() ) );
        return resp;
    }

}
