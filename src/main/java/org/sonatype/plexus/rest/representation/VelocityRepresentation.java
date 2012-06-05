/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.sonatype.sisu.velocity.Velocity;

public class VelocityRepresentation
    extends TemplateRepresentation
{
    private final VelocityEngine velocityEngine;

    /**
     * Constructor when template is on core classpath (will be loaded using VelocityEngine).
     * 
     * @param context
     * @param templateName
     * @param dataModel
     * @param mediaType
     */
    public VelocityRepresentation( Context context, String templateName, Map<String, Object> dataModel,
                                   MediaType mediaType )
    {
        super( getTemplate( context, templateName ), dataModel, mediaType );
        this.velocityEngine = getEngine( context );
    }

    /**
     * Constructor when template is got from somewhere else then core (ie, from a template NOT on core classpath).
     * 
     * @param context
     * @param templateName
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
     * Nonsense constructor... Velocity template without data model?
     * 
     * @param context
     * @param templateName
     * @param mediaType
     * @deprecated use other constructors.
     */
    @Deprecated
    public VelocityRepresentation( Context context, String templateName, MediaType mediaType )
    {
        super( getTemplate( context, templateName ), mediaType );
        this.velocityEngine = getEngine( context );
    }

    // ==

    @Override
    public VelocityEngine getEngine()
    {
        return velocityEngine;
    }

    // ==

    private static Template getTemplate( final Context context, final String templateName )
    {
        try
        {
            return getEngine( context ).getTemplate( templateName );
        }
        catch ( Exception e )
        {
            throw new IllegalArgumentException( "Cannot get the template with name " + String.valueOf( templateName ),
                e );
        }
    }

    private static VelocityEngine getEngine( final Context context )
    {
        return ( (Velocity) context.getAttributes().get( Velocity.class.getName() ) ).getEngine();
    }
}
