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
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResourceResponse;
import org.sonatype.nexus.templates.Template;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryTypesComponentListPlexusResource" )
public class RepositoryTypesComponentListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Requirement
    private RepositoryTypeRegistry repoTypeRegistry;
    
    @Override
    public String getResourceUri()
    {
        return "/components/repo_types";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:componentsrepotypes]" );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        // such horrible terminology for this class, its actually repo providers that are being returned
        String repoType = form.getFirstValue( "repoType" );
        
        TemplateSet templateSet = getNexus().getRepositoryTemplates();
        
        if ( "hosted".equals( repoType ) )
        {
            templateSet = templateSet.getTemplates( HostedRepository.class );
        }
        else if ( "proxy".equals( repoType ) )
        {
            templateSet = templateSet.getTemplates( ProxyRepository.class );
        }
        else if ( "shadow".equals( repoType ) )
        {
            templateSet = templateSet.getTemplates( ShadowRepository.class );
        }
        else if ( "group".equals( repoType ) )
        {
            templateSet = templateSet.getTemplates( GroupRepository.class );
        }
        
        NexusRepositoryTypeListResourceResponse result = new NexusRepositoryTypeListResourceResponse();
        
        if ( templateSet.getTemplatesList().isEmpty() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }
        
        for ( Template template : templateSet.getTemplatesList() )
        {
            NexusRepositoryTypeListResource resource = new NexusRepositoryTypeListResource();
            
            String providerRole = ( ( RepositoryTemplate ) template ).getRepositoryProviderRole();
            String providerHint = ( ( RepositoryTemplate ) template ).getRepositoryProviderHint();
            
            resource.setProvider( providerHint );

            resource.setFormat( ( ( AbstractRepositoryTemplate ) template ).getContentClass().getId() );
            
            resource.setDescription( repoTypeRegistry.getRepositoryDescription( providerRole, providerHint ) );

            // add it to the collection
            result.addData( resource );            
        }

        return result;
    }
}
