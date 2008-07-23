/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security.runtime;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.configuration.security.NexusSecurityConfiguration;

/**
 * The Class DefaultRuntimeConfigurationBuilder. Todo: all the bad thing is now concentrated in this class. We are
 * playing container instead of container. TODO: Slim this class and rely onto Plexus!
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultSecurityRuntimeConfigurationBuilder
    extends AbstractLogEnabled
    implements SecurityRuntimeConfigurationBuilder, Contextualizable
{
    public static final String DEFAULT_LS_PROVIDER = "file";

    private PlexusContainer plexusContainer;

    private NexusSecurityConfiguration nexusConfiguration;

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize( NexusSecurityConfiguration configuration )
    {
        this.nexusConfiguration = configuration;
    }
}
