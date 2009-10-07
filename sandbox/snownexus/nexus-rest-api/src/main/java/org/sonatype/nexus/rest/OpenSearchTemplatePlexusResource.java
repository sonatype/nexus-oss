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
package org.sonatype.nexus.rest;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.representation.VelocityRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "openSearchTemplate" )
public class OpenSearchTemplatePlexusResource
    extends AbstractNexusPlexusResource
{

    public OpenSearchTemplatePlexusResource()
    {
        super();
        setReadable( true );
        setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        // RO resource
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/opensearch";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // the client should have index access for the search to work
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:index]" );
    }

    public Representation get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Map<String, Object> map = new HashMap<String, Object>();


        Reference nexusRef = getContextRoot( request );
        String nexusRoot = nexusRef.toString();
        if ( nexusRoot.endsWith( "/" ) ) {
        	nexusRoot = nexusRoot.substring( 0, nexusRoot.length() - 1 );
        }
        
        map.put( "nexusRoot", nexusRoot );
        map.put( "nexusHost", nexusRef.getHostDomain() );

        VelocityRepresentation templateRepresentation = new VelocityRepresentation(
            context, "/templates/opensearch.vm", map, MediaType.TEXT_XML );

        return templateRepresentation;
    }
}
