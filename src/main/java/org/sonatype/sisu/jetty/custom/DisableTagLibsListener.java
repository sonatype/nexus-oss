/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty.custom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Listener to disable taglibs on a webapp. There were problems with Jetty6 when it was trying to "reach out" to get the
 * DTDs of it's default taglibs and in isolated environments it caused long pauses on boot.
 * 
 * @author jdcasey
 */
public class DisableTagLibsListener
    implements LifeCycle.Listener
{
    public void lifeCycleStarting( LifeCycle lifecycle )
    {
        if ( lifecycle instanceof ContextHandlerCollection )
        {
            disableTagLibs( (ContextHandlerCollection) lifecycle );
        }
    }

    public void lifeCycleStarted( LifeCycle lifecycle )
    {
        // NOP
    }

    public void lifeCycleStopping( LifeCycle lifecycle )
    {
        // NOP
    }

    public void lifeCycleStopped( LifeCycle lifecycle )
    {
        // NOP
    }

    public void lifeCycleFailure( LifeCycle lifecycle, Throwable cause )
    {
        // NOP
    }

    // ==

    private void disableTagLibs( ContextHandlerCollection collection )
    {
        Logger logger = Log.getLogger( getClass().getName() );

        Handler[] childHandlers = collection.getChildHandlers();

        if ( childHandlers != null && childHandlers.length > 0 )
        {
            for ( Handler child : childHandlers )
            {
                if ( child instanceof WebAppContext )
                {
                    WebAppContext webapp = (WebAppContext) child;

                    if ( logger != null )
                    {
                        logger.info( "Disabling TLD support for WebAppContext on context path: {}", webapp.getContextPath() );
                    }

                    Configuration[] configs = webapp.getConfigurations();
                    if ( configs != null )
                    {
                        List<Configuration> toSet = new ArrayList<Configuration>();

                        for ( int i = 0; i < configs.length; i++ )
                        {
                            if ( !( configs[i] instanceof TagLibConfiguration ) )
                            {
                                toSet.add( configs[i] );
                            }
                        }

                        boolean hasDisabledTaglibs = false;
                        for ( Configuration configuration : toSet )
                        {
                            if ( configuration instanceof DisabledTagLibConfiguration )
                            {
                                hasDisabledTaglibs = true;
                                break;
                            }
                        }

                        if ( !hasDisabledTaglibs )
                        {
                            toSet.add( new DisabledTagLibConfiguration() );
                        }

                        webapp.setConfigurations( toSet.toArray( configs ) );
                    }
                    else
                    {
                        String[] configClasses = webapp.getConfigurationClasses();
                        if ( configClasses == null )
                        {
                            configClasses = new String[1];
                            configClasses[0] = DisabledTagLibConfiguration.class.getName();
                        }
                        else
                        {
                            List<String> toSet = new ArrayList<String>();
                            for ( int i = 0; i < configClasses.length; i++ )
                            {
                                toSet.add( configClasses[i] );
                            }

                            toSet.remove( TagLibConfiguration.class.getName() );

                            if ( !toSet.contains( DisabledTagLibConfiguration.class.getName() ) )
                            {
                                toSet.add( DisabledTagLibConfiguration.class.getName() );
                            }

                            configClasses = toSet.toArray( configClasses );
                        }

                        webapp.setConfigurationClasses( configClasses );
                    }

                    if ( logger != null && logger.isDebugEnabled() )
                    {
                        StringBuilder builder = new StringBuilder();
                        if ( webapp.getConfigurations() != null )
                        {
                            for ( Configuration configuration : webapp.getConfigurations() )
                            {
                                builder.append( "\n" ).append( configuration.getClass().getName() );
                            }

                            logger.debug( "\n\nThe following configurations are in use for this webapp: {}", builder,
                                null );
                        }

                        builder.setLength( 0 );
                        if ( webapp.getConfigurationClasses() != null )
                        {
                            for ( String configClass : webapp.getConfigurationClasses() )
                            {
                                builder.append( "\n" ).append( configClass );
                            }

                            logger.debug( "\n\nThe following configurationClasses are in use for this webapp: {}",
                                builder, null );
                        }
                    }
                }
            }
        }
    }

    public static class DisabledTagLibConfiguration
        extends TagLibConfiguration
    {
        @Override
        public void preConfigure( WebAppContext context )
            throws Exception
        {
            // Disable this from the superclass.
        }
    }
}
