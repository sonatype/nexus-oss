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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.restlet.Application;
import org.restlet.Context;

import com.noelios.restlet.http.HttpRequest;
import com.noelios.restlet.http.HttpResponse;
import com.noelios.restlet.http.HttpServerConverter;

/**
 * Jetty handler that converts Jetty calls into restlet.org ones and invokes target to handle them. How to use: register
 * this handler into existing Jetty instance (use plexus-jetty6 for that). You _have_ to register a plexus component
 * with role "org.sonatype.plexus.rest.PlexusRestletApplicationBridge" in you App as extension of
 * PlexusRestletApplicationBridge abstract class found here, and implement restlet.org "wiring"/root creation.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.plexus.rest.JettyRestletHandler"
 */
public class JettyRestletHandler
    extends AbstractHandler
    implements LogEnabled
{

    /** @plexus.configuration default-value="/" */
    private String handledPath;

    /** @plexus.configuration */
    private String applicationClassName;

    /** Helper from restlet.org to convert server calls to restlet.org calls */
    private volatile HttpServerConverter httpServerConverter;

    private RestletOrgApplication application;

    private Logger logger;

    public Logger getLogger()
    {
        return logger;
    }

    public RestletOrgApplication getApplication()
    {
        if ( application == null )
        {
            try
            {
                Class applicationClass = Class.forName( applicationClassName, false, Thread
                    .currentThread().getContextClassLoader() );

                Context context = new Context();

                Application app = null;

                try
                {
                    app = (Application) applicationClass.getConstructor( Context.class ).newInstance( context );
                }
                catch ( NoSuchMethodException e )
                {
                    getLogger()
                        .warn(
                            "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context. The empty constructor and the context setter will be used instead. "
                                + applicationClassName,
                            e );
                    app = (Application) applicationClass.getConstructor().newInstance();

                    app.setContext( context );
                }

                application = (RestletOrgApplication) app;
            }
            catch ( ClassNotFoundException e )
            {
                getLogger().error(
                    "No RestletOrgApplication of class " + applicationClassName + " found in classpath!",
                    e );
            }
            catch ( Exception e )
            {
                getLogger().error( "Could not instantaniate RestletOrgApplication of class " + applicationClassName, e );
            }

        }
        return application;
    }

    public void setApplication( RestletOrgApplication application )
    {
        this.application = application;
    }

    public String getHandledPath()
    {
        return handledPath;
    }

    public void setHandledPath( String handledPath )
    {
        this.handledPath = handledPath;
    }

    public HttpServerConverter getHttpServerConverter()
    {
        if ( httpServerConverter == null )
        {
            this.httpServerConverter = new HttpServerConverter( getApplication().getContext() );
        }
        return httpServerConverter;
    }

    public void setHttpServerConverter( HttpServerConverter httpServerConverter )
    {
        this.httpServerConverter = httpServerConverter;
    }

    /**
     * We convert here the incoming Jetty requests to restlet.org ServerCall, and then using the converter to
     * HttpRequest and HttpResponse. Finally, we simply feed those to the Application.
     */
    public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
        throws IOException,
            ServletException
    {
        if ( target != null && target.startsWith( getHandledPath() ) )
        {
            HttpConnection httpConnection = ( (Request) request ).getConnection();

            String uri = target.substring( getHandledPath().length() );

            JettyServerCall httpCall = new JettyServerCall( httpConnection, uri );

            try
            {
                HttpRequest httpRequest = getHttpServerConverter().toRequest( httpCall );

                HttpResponse httpResponse = new HttpResponse( httpCall, httpRequest );

                getApplication().handle( httpRequest, httpResponse );

                getHttpServerConverter().commit( httpResponse );
            }
            catch ( Exception e )
            {
                throw new ServletException( e );
            }
        }

    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

}
