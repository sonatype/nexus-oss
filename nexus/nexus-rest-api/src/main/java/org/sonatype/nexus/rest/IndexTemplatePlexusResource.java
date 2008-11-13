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
import org.codehaus.plexus.component.annotations.Requirement;
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
    implements ManagedPlexusResource
{
    @Requirement
    private Nexus nexus;

    @Requirement( role = NexusResourceBundle.class )
    private Map<String, NexusResourceBundle> bundles;

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

        VelocityRepresentation templateRepresentation = new VelocityRepresentation(
            context,
            "/templates/index.vm",
            MediaType.TEXT_HTML );

        // gather plugin stuff

        Map<String, Object> topContext = new HashMap<String, Object>( templatingContext );

        Map<String, Object> pluginContext = null;

        List<String> pluginHeadContributions = new ArrayList<String>();

        List<String> pluginBodyContributions = new ArrayList<String>();

        for ( String key : bundles.keySet() )
        {
            pluginContext = new HashMap<String, Object>( topContext );

            NexusResourceBundle bundle = bundles.get( key );

            pluginContext.put( "bundle", bundle );

            // HEAD

            String headTemplate = bundle.getHeadContribution( pluginContext );

            evaluateIfNeeded( templateRepresentation.getEngine(), pluginContext, headTemplate, pluginHeadContributions );

            // BODY

            String bodyTemplate = bundle.getBodyContribution( pluginContext );

            evaluateIfNeeded( templateRepresentation.getEngine(), pluginContext, bodyTemplate, pluginBodyContributions );
        }

        templatingContext.put( "pluginHeadContributions", pluginHeadContributions );

        templatingContext.put( "pluginBodyContributions", pluginBodyContributions );

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
}
