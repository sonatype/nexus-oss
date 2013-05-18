/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.rest.representation;

import java.util.Collections;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.sisu.velocity.Velocity;

/**
 * Velocity representation that enhances Restlet's {@link TemplateRepresentation}, that Velocity instance is reused.
 * Problem with Restlet {@link TemplateRepresentation} is that it creates a new instance of Velocity per creation of
 * {@link TemplateRepresentation}. This class remedies that, by overriding how {@link VelocityEngine} is obtained, as
 * Plexus Application will stuff a VelocityEngine provider into context, hence, a singleton instance of Velocity will be
 * reused. See SISU {@link Velocity} that is used under the hub.
 * 
 * @author cstamas
 */
public class VelocityRepresentation
    extends TemplateRepresentation
{
    /**
     * The engine instance to be used to render this {@link VelocityRepresentation}.
     */
    private final VelocityEngine velocityEngine;

    /**
     * Constructor when Template is already assembled.
     * 
     * @param context
     * @param template
     * @param dataModel
     * @param mediaType
     * @since 1.21
     */
    public VelocityRepresentation( Context context, Template template, Map<String, Object> dataModel,
                                   MediaType mediaType )
    {
        super( template, dataModel, mediaType );
        this.velocityEngine = getEngine( context );
    }

    /**
     * Constructor when template to use comes from some other classloader than the one where this class is.
     * 
     * @param context
     * @param templateName
     * @param cl
     * @param dataModel
     * @param mediaType
     * @since 1.23
     */
    public VelocityRepresentation( Context context, String templateName, ClassLoader cl, Map<String, Object> dataModel,
                                   MediaType mediaType )
    {
        this( context, getTemplate( context, templateName, cl ), dataModel, mediaType );
    }

    /**
     * Constructor when template is on core classpath (will be loaded using VelocityEngine). This constructor accepts
     * template name (binary name), and will use current classloader to locate the template. This constructor is not
     * quite usable in apps that maintains multiple classloaders, as there is no control exposed over classloader to be
     * used to load up the template resource.
     * 
     * @param context
     * @param templateName
     * @param dataModel
     * @param mediaType
     * @deprecated Use the constructor that accepts {@link Template}, as it gives you total control how to obtain the
     *             template (ie. to use custom classloader or so).
     */
    public VelocityRepresentation( Context context, String templateName, Map<String, Object> dataModel,
                                   MediaType mediaType )
    {
        this( context, getTemplate( context, templateName, VelocityRepresentation.class.getClassLoader() ), dataModel,
            mediaType );
    }

    /**
     * Nonsense constructor... Velocity template without data model? This constructor is not quite usable in apps that
     * maintains multiple classloaders, as there is no control exposed over classloader to be used to load up the
     * template resource.
     * 
     * @param context
     * @param templateName
     * @param mediaType
     * @deprecated Use other constructors, as this one is a bit nonsense.
     */
    @Deprecated
    public VelocityRepresentation( Context context, String templateName, MediaType mediaType )
    {
        this( context, getTemplate( context, templateName, VelocityRepresentation.class.getClassLoader() ),
            Collections.<String, Object> emptyMap(), mediaType );
    }

    // ==

    /**
     * We return our own managed velocity engine instance, to avoid Restlet create one.
     */
    @Override
    public VelocityEngine getEngine()
    {
        return velocityEngine;
    }

    // ==

    /**
     * Helper method to obtain {@link Template} instances, with explicit control what {@link ClassLoader} needs to be
     * used to locate it.
     * 
     * @param context
     * @param templateName
     * @param cl
     * @return the {@link Template} instance
     */
    public static Template getTemplate( final Context context, final String templateName, final ClassLoader cl )
    {
        // NOTE: Velocity's ClasspathResourceLoader goes for TCCL 1st, then would fallback to "system"
        // (in this case the classloader where Velocity is loaded) classloader
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( cl );
        try
        {
            return getEngine( context ).getTemplate( templateName );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( "Cannot get the template with name " + String.valueOf( templateName ),
                e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( original );
        }
    }

    /**
     * {@link PlexusRestletApplicationBridge} stuffs the SISU {@link Velocity} into context, and we use the shared
     * instance from it, instead to recreate it over and over again.
     * 
     * @param context
     * @return
     */
    private static VelocityEngine getEngine( final Context context )
    {
        return ( (Velocity) context.getAttributes().get( Velocity.class.getName() ) ).getEngine();
    }
}
