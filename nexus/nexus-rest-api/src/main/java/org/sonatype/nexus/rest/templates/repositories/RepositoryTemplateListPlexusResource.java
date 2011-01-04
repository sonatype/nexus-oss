/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.templates.repositories;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.templates.Template;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTemplateListPlexusResource" )
@Path( RepositoryTemplateListPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class RepositoryTemplateListPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String RESOURCE_URI = "/templates/repositories";
    
    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repotemplates]" );
    }

    /**
     * Retrieve a list of repository templates in nexus.  Some default configurations for common repository types.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = RepositoryListResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryListResourceResponse result = new RepositoryListResourceResponse();

        RepositoryListResource repoRes;

        TemplateSet repoTemplates = getNexus().getRepositoryTemplates();

        for ( Template tmp : repoTemplates )
        {
            RepositoryTemplate template = (RepositoryTemplate) tmp;

            repoRes = new RepositoryListResource();

            repoRes.setResourceURI( createChildReference( request, this, template.getId() ).toString() );

            repoRes.setId( template.getId() );

            repoRes.setName( template.getDescription() );

            if ( ProxyRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "proxy" );
            }
            else if ( HostedRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "hosted" );
            }
            else if ( ShadowRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "virtual" );
            }
            else if ( GroupRepository.class.isAssignableFrom( template.getMainFacet() ) )
            {
                repoRes.setRepoType( "group" );
            }
            else
            {
                // huh?
                repoRes.setRepoType( template.getMainFacet().getName() );
            }

            // policy
            // another hack
            if ( template.getCoreConfiguration().getExternalConfiguration().getConfiguration( false ) instanceof AbstractMavenRepositoryConfiguration )
            {
                repoRes.setRepoPolicy( ( (AbstractMavenRepositoryConfiguration) template.getCoreConfiguration()
                    .getExternalConfiguration().getConfiguration( false ) ).getRepositoryPolicy().toString() );
            }

            // format
            repoRes.setFormat( template.getContentClass().getId() );

            // userManaged
            repoRes.setUserManaged( template.getConfigurableRepository().isUserManaged() );

            // exposed
            repoRes.setExposed( template.getConfigurableRepository().isExposed() );

            // ==
            // below are not used for templates (and does not make any sense)
            // effectiveLocalStorageUrl
            // remoteUri

            result.addData( repoRes );
        }

        return result;
    }
}