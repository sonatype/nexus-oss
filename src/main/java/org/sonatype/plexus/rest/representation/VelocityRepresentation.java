/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
