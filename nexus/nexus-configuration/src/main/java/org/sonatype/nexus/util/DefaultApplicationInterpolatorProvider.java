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
package org.sonatype.nexus.util;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * A simple class that holds Regex interpolator and has reference to Plexus context too, to centralize Plexus coupling
 * but make application Plexus interpolation capable too. This interpolator interpolates with Plexus Context,
 * Environment variables and System Properties, in this order.
 * 
 * @author cstamas
 */
@Component( role = ApplicationInterpolatorProvider.class )
public class DefaultApplicationInterpolatorProvider
    implements ApplicationInterpolatorProvider, Contextualizable
{
    private RegexBasedInterpolator regexBasedInterpolator;

    public DefaultApplicationInterpolatorProvider()
    {
        super();

        regexBasedInterpolator = new RegexBasedInterpolator();
    }

    public Interpolator getInterpolator()
    {
        return regexBasedInterpolator;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        regexBasedInterpolator.addValueSource( new MapBasedValueSource( context.getContextData() ) );

        // FIXME: bad, everything should come from Plexus context
        regexBasedInterpolator.addValueSource( new MapBasedValueSource( System.getenv() ) );

        // FIXME: bad, everything should come from Plexus context
        regexBasedInterpolator.addValueSource( new MapBasedValueSource( System.getProperties() ) );
    }

}
