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
import org.restlet.Application;
import org.restlet.Context;

import com.noelios.restlet.ext.servlet.ServerServlet;

/**
 * Another Plexus to restlet.org bridge, usable in Servlet environment. This class is only extending ServletServer in
 * way that the Plexus container is pushed into Context, to make it available to other Plexus aware restlets.
 * 
 * @author cstamas
 */
public class ServletRestletApplicationBridge
    extends ServerServlet
{
    public Application createApplication( Context context )
    {
        // a Plexus aware implementation of createApplication()
        Application application = super.createApplication( context );

        if ( PlexusRestletApplicationBridge.class.isAssignableFrom( application.getClass() ) )
        {
            ( (PlexusRestletApplicationBridge) application ).setPlexusContainer( getPlexusContainer() );
        }
        
        return application;
    }

    protected PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getServletContext().getAttribute( PlexusConstants.PLEXUS_KEY );
    }

}
