/**
  * Copyright (C) 2008 Sonatype Inc. 
  * Sonatype Inc, licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in 
  * compliance with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
package org.sonatype.plexus.rest;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * Helper base class for restlet resources that needs Plexus Context or Container access.
 * 
 * @author cstamas
 */
public abstract class AbstractPlexusAwareResource
    extends Resource
{

    /**
     * Default Resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractPlexusAwareResource( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * Returns the Plexus Container from context.
     * 
     * @return the container
     */
    protected PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getContext().getAttributes().get( PlexusConstants.PLEXUS_KEY );
    }

}
