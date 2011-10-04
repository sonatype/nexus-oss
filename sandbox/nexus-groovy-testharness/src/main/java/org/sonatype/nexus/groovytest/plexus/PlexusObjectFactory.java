/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.groovytest.plexus;

import java.lang.reflect.Constructor;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.testng.IObjectFactory;

public class PlexusObjectFactory
    implements IObjectFactory
{

    private PlexusContainer container;

    public PlexusObjectFactory()
        throws PlexusContainerException
    {
        container = new DefaultPlexusContainer();
    }

    private static final long serialVersionUID = -45456541236971L;

    public Object newInstance( Constructor constructor, Object... params )
    {
        String role = constructor.getDeclaringClass().getName();
        String hint = null;
        if ( params != null && params.length == 1 && params[0] instanceof String )
        {
            hint = (String) params[0];
        }

        try
        {
            if ( hint != null )
            {
                return container.lookup( role, hint );
            }
            else
            {
                return container.lookup( role );
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( e );
        }

    }

}
