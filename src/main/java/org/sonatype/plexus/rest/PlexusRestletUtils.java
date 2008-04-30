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

/**
 * A collection of short static snippets.
 * 
 * @author cstamas
 */
public class PlexusRestletUtils
{

    public static Object plexusLookup( Context context, String role )
    {
        try
        {
            PlexusContainer container = (PlexusContainer) context.getAttributes().get( PlexusConstants.PLEXUS_KEY );

            return container.lookup( role );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    public static Object plexusLookup( Context context, String role, String roleHint )
    {
        try
        {
            PlexusContainer container = (PlexusContainer) context.getAttributes().get( PlexusConstants.PLEXUS_KEY );

            return container.lookup( role, roleHint );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

}
