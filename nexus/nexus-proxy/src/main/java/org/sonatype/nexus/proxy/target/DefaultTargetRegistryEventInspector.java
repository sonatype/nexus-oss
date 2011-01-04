/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.target;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "DefaultTargetRegistryEventInspector" )
public class DefaultTargetRegistryEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private TargetRegistry targetRegistry;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof NexusStartedEvent );
    }

    public void inspect( Event<?> evt )
    {
        try
        {
            boolean changed = false;

            Map<String, ContentClass> contentClasses = repositoryTypeRegistry.getContentClasses();

            for ( String key : contentClasses.keySet() )
            {
                boolean found = false;

                for ( Target target : targetRegistry.getTargetsForContentClass( contentClasses.get( key ) ) )
                {
                    // create default target for each content class that doesn't already exist
                    if ( target.getContentClass().equals( contentClasses.get( key ) )
                        && target.getPatternTexts().size() == 1
                        && target.getPatternTexts().iterator().next().equals( ".*" ) )
                    {
                        found = true;
                        break;
                    }
                }

                if ( !found )
                {
                    Target newTarget =
                        new Target( key, "All (" + key + ")", contentClasses.get( key ), Collections.singleton( ".*" ) );

                    targetRegistry.addRepositoryTarget( newTarget );
                    changed = true;
                    getLogger().info( "Adding default target for " + key + " content class" );
                }
            }

            if ( changed )
            {
                applicationConfiguration.saveConfiguration();
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to properly add default Repository Targets", e );
        }
        catch ( ConfigurationException e )
        {
            getLogger().error( "Unable to properly add default Repository Targets", e );
        }
    }
}
