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

import org.codehaus.plexus.PlexusContainer;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * This is a marker interface to be implemented by one and only Plexus instantianated Restlet.org Application, that will
 * be injected into JettyRestletHandler. Since restlet.org has never heard about interfaces, this is a little trick to
 * publish methods that should be actually provided by implementing org.restlet.Application class.
 * 
 * @author cstamas
 */
public interface RestletOrgApplication
{

    /** See org.restlet.Application.getContext() */
    Context getContext();

    /** See org.restlet.Application.handle(request, response) */
    void handle( Request request, Response response );

    PlexusContainer getPlexusContainer();

}
