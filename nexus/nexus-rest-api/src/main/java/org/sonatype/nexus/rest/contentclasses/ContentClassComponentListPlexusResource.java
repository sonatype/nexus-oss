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
package org.sonatype.nexus.rest.contentclasses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ContentClassComponentListPlexusResource" )
@Path( ContentClassComponentListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class ContentClassComponentListPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/components/repo_content_classes";
    
    @Requirement
    private RepositoryTypeRegistry repoTypeRegistry;

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:componentscontentclasses]" );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }
    
    /**
     * Retrieve the list of content classes availabe in nexus.  Plugins can contribute to this list.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RepositoryContentClassListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {   
        RepositoryContentClassListResourceResponse contentClasses = new RepositoryContentClassListResourceResponse();
        
        for ( ContentClass contentClass : repoTypeRegistry.getContentClasses().values() )
        {
            RepositoryContentClassListResource resource = new RepositoryContentClassListResource();
            resource.setContentClass( contentClass.getId() );
            resource.setName( contentClass.getName() );
            resource.setGroupable( contentClass.isGroupable() );
            
            for ( String compClass : repoTypeRegistry.getCompatibleContentClasses( contentClass ) )
            {
                resource.addCompatibleType( compClass );
            }
            
            contentClasses.addData( resource );
        }
        
        return contentClasses;
    }
}
