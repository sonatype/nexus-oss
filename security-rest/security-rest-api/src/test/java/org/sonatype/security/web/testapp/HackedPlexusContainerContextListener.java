/**
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
package org.sonatype.security.web.testapp;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.plexus.rest.PlexusContainerContextListener;


public class HackedPlexusContainerContextListener
    extends PlexusContainerContextListener
{

    private static ThreadLocal<PlexusContainer> plexusContainer = new ThreadLocal<PlexusContainer>();
    
    
//    @Override
//    public void contextInitialized( ServletContextEvent sce )
//    {
//        SLF4JBridgeHandler.install();
//        super.contextInitialized( sce );
//    }

    @Override
    public void contextDestroyed( ServletContextEvent sce )
    {
        SLF4JBridgeHandler.uninstall();
    }


    @Override
    protected void initizlizePlexusContainer( ServletContext context, ContainerConfiguration configuration )
        throws PlexusContainerException
    {
        context.setAttribute( "plexus", this.plexusContainer.get());
    }


    public PlexusContainer getPlexusContainer()
    {
        return plexusContainer.get();
    }


    public void setPlexusContainer( PlexusContainer plexusContainer )
    {
        this.plexusContainer.set( plexusContainer );
    }

}
