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
