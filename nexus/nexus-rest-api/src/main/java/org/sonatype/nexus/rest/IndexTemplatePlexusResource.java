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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.plexus.rest.representation.VelocityRepresentation;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Component( role = ManagedPlexusResource.class, hint = "indexTemplate" )
public class IndexTemplatePlexusResource
    extends AbstractPlexusResource
    implements ManagedPlexusResource,
        Initializable
{
    @Requirement
    private Nexus nexus;

    @Requirement( role = NexusResourceBundle.class )
    private Map<String, NexusResourceBundle> bundles;
    
    @Configuration( value = "${index.template.file}" )
    String templateFilename;

    public IndexTemplatePlexusResource()
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
        return "/index.html";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // unprotected
        return null;
    }

    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.APPLICATION_XHTML_XML ) );

        return result;
    }

    public Representation get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return render( context, request, response, variant );
    }

    protected VelocityRepresentation render( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Map<String, Object> templatingContext = new HashMap<String, Object>();

        templatingContext.put( "serviceBase", "service/local" );

        templatingContext.put( "contentBase", "content" );

        templatingContext.put( "nexusVersion", nexus.getSystemStatus().getVersion() );

        templatingContext.put( "nexusRoot", request.getRootRef().toString() );

        templatingContext.put( "nexusHost", request.getHostRef().getHostDomain() );

        VelocityRepresentation templateRepresentation = new VelocityRepresentation(
            context,
            templateFilename,
            MediaType.TEXT_HTML );

        // gather plugin stuff

        Map<String, Object> topContext = new HashMap<String, Object>( templatingContext );

        Map<String, Object> pluginContext = null;

        List<String> pluginPreHeadContributions = new ArrayList<String>();
        List<String> pluginPostHeadContributions = new ArrayList<String>();

        List<String> pluginPreBodyContributions = new ArrayList<String>();
        List<String> pluginPostBodyContributions = new ArrayList<String>();

        for ( String key : bundles.keySet() )
        {
            pluginContext = new HashMap<String, Object>( topContext );

            NexusResourceBundle bundle = bundles.get( key );

            pluginContext.put( "bundle", bundle );

            // pre HEAD

            String preHeadTemplate = bundle.getPreHeadContribution( pluginContext );

            evaluateIfNeeded(
                templateRepresentation.getEngine(),
                pluginContext,
                preHeadTemplate,
                pluginPreHeadContributions );

            // post HEAD

            String postHeadTemplate = bundle.getPostHeadContribution( pluginContext );

            evaluateIfNeeded(
                templateRepresentation.getEngine(),
                pluginContext,
                postHeadTemplate,
                pluginPostHeadContributions );

            // pre BODY

            String preBodyTemplate = bundle.getPreBodyContribution( pluginContext );

            evaluateIfNeeded(
                templateRepresentation.getEngine(),
                pluginContext,
                preBodyTemplate,
                pluginPreBodyContributions );

            // post BODY

            String postBodyTemplate = bundle.getPreBodyContribution( pluginContext );

            evaluateIfNeeded(
                templateRepresentation.getEngine(),
                pluginContext,
                postBodyTemplate,
                pluginPostBodyContributions );
        }
        
        templatingContext.put( "appName", nexus.getSystemStatus().getAppName() );
        templatingContext.put( "formattedAppName", nexus.getSystemStatus().getFormattedAppName() );

        templatingContext.put( "pluginPreHeadContributions", pluginPreHeadContributions );
        templatingContext.put( "pluginPostHeadContributions", pluginPostHeadContributions );

        templatingContext.put( "pluginPreBodyContributions", pluginPreBodyContributions );
        templatingContext.put( "pluginPostBodyContributions", pluginPostBodyContributions );

        templateRepresentation.setDataModel( templatingContext );

        return templateRepresentation;
    }

    protected void evaluateIfNeeded( VelocityEngine engine, Map<String, Object> context, String template,
        List<String> results )
        throws ResourceException
    {
        if ( !StringUtils.isEmpty( template ) )
        {
            StringWriter result = new StringWriter();

            try
            {
                if ( engine.evaluate( new VelocityContext( context ), result, getClass().getName(), template ) )
                {
                    results.add( result.toString() );
                }
                else
                {
                    throw new ResourceException(
                        Status.SERVER_ERROR_INTERNAL,
                        "Was not able to interpolate (check the logs for Velocity messages about the reason)!" );
                }
            }
            catch ( IOException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got IO exception during Velocity invocation!",
                    e );
            }
            catch ( ParseErrorException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got ParseErrorException exception during Velocity invocation!",
                    e );
            }
            catch ( MethodInvocationException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got MethodInvocationException exception during Velocity invocation!",
                    e );
            }
            catch ( ResourceNotFoundException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got ResourceNotFoundException exception during Velocity invocation!",
                    e );
            }
        }
    }
    
    public void initialize()
        throws InitializationException
    {
        //Hasn't been interpolated
        if ( "${index.template.file}".equals( templateFilename ) )
        {
            templateFilename = "templates/index.vm";
        }
    }
}
