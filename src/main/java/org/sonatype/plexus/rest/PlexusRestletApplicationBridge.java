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

/**
 * An abstract application, that should be extended and registered as Plexus component. Extended it to configure root,
 * as you would do with org.restlet.Application. This class simply places a Plexus Context into restlet.org Application
 * context to make plexus reachable from restlets.
 * 
 * @author cstamas
 */
public abstract class PlexusRestletApplicationBridge
    extends Application
    implements RestletOrgApplication
{
    public PlexusContainer getPlexusContainer()
    {
        return (PlexusContainer) getContext().getAttributes().get( PlexusConstants.PLEXUS_KEY );
    }

    public void setPlexusContainer( PlexusContainer plexusContainer )
    {
        getContext().getAttributes().put( PlexusConstants.PLEXUS_KEY, plexusContainer );
    }

    public PlexusRestletApplicationBridge( Context context )
    {
        super( context );

        setConverterService( new PlexusConverterService() );
    }
}
