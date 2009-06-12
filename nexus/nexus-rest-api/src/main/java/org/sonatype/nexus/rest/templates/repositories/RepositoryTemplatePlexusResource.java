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
package org.sonatype.nexus.rest.templates.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.template.RepositoryTemplateProvider;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryTemplatePlexusResource" )
public class RepositoryTemplatePlexusResource
    extends AbstractNexusPlexusResource
{
    private static final String TEMPLATE_ID_KEY = "templateId";

    @Requirement
    private RepositoryTemplateProvider templateStore;

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/templates/repositories/{" + TEMPLATE_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/templates/repositories/*", "authcBasic,perms[nexus:repotemplates]" );
    }

    protected String getTemplateId( Request request )
    {
        return request.getAttributes().get( TEMPLATE_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryResourceResponse result = new RepositoryResourceResponse();

        String templateId = getTemplateId( request );
        RepositoryBaseResource template = templateStore.retrieveTemplate( templateId );

        if ( template == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository template not found: " );
        }

        result.setData( template );

        return result;
    }

}
