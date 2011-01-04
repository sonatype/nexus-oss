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
package org.sonatype.security.ldap.upgrade.cipher;

import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CryptoUtils
{
    /**
     * Exploratory part. This method returns all available services types
     */
    public static String[] getServiceTypes()
    {
        Set<String> result = new HashSet<String>();

        // All all providers
        Provider[] providers = Security.getProviders();
        for ( int i = 0; i < providers.length; i++ )
        {
            // Get services provided by each provider
            Set<Object> keys = providers[i].keySet();
            for ( Iterator<Object> it = keys.iterator(); it.hasNext(); )
            {
                String key = (String) it.next();
                key = key.split( " " )[0];

                if ( key.startsWith( "Alg.Alias." ) )
                {
                    // Strip the alias
                    key = key.substring( 10 );
                }
                int ix = key.indexOf( '.' );
                result.add( key.substring( 0, ix ) );
            }
        }
        return result.toArray( new String[result.size()] );
    }

    /**
     * This method returns the available implementations for a service type
     */
    public static String[] getCryptoImpls( String serviceType )
    {
        Set<String> result = new HashSet<String>();

        // All all providers
        Provider[] providers = Security.getProviders();
        for ( int i = 0; i < providers.length; i++ )
        {
            // Get services provided by each provider
            Set<Object> keys = providers[i].keySet();
            for ( Iterator<Object> it = keys.iterator(); it.hasNext(); )
            {
                String key = (String) it.next();
                key = key.split( " " )[0];

                if ( key.startsWith( serviceType + "." ) )
                {
                    result.add( key.substring( serviceType.length() + 1 ) );
                }
                else if ( key.startsWith( "Alg.Alias." + serviceType + "." ) )
                {
                    // This is an alias
                    result.add( key.substring( serviceType.length() + 11 ) );
                }
            }
        }
        return result.toArray( new String[result.size()] );
    }
}
