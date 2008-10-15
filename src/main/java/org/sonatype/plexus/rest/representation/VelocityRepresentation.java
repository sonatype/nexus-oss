package org.sonatype.plexus.rest.representation;

import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.sonatype.logging.RestletLogChute;

public class VelocityRepresentation
    extends TemplateRepresentation
{
    public VelocityRepresentation( Context context, String templateName, Map<String, Object> dataModel, MediaType mediaType )
    {
        super( templateName, dataModel, mediaType );
        configureEngine( context );
    }

    public VelocityRepresentation( Context context, String templateName, MediaType mediaType )
    {
        super( templateName, mediaType );
        configureEngine( context );
    }

    protected void configureEngine( Context context )
    {
        VelocityEngine engine = getEngine();
        
        engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new RestletLogChute( context ) );

        engine.setProperty( RuntimeConstants.RESOURCE_LOADER, "class" );

        engine.setProperty(
            "class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
    }

}
