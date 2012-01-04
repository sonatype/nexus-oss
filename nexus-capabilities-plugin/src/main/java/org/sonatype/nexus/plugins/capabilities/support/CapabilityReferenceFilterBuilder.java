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
package org.sonatype.nexus.plugins.capabilities.support;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import com.google.common.base.Predicate;

public class CapabilityReferenceFilterBuilder
{

    public static CapabilityReferenceFilter capabilities()
    {
        return new CapabilityReferenceFilter();
    }

    public static class CapabilityReferenceFilter
        implements Predicate<CapabilityReference>
    {

        private String typeId;

        private Map<String, String> properties = new HashMap<String, String>();

        public CapabilityReferenceFilter withType( final CapabilityType type )
        {
            typeId = type.toString();
            return this;
        }

        public CapabilityReferenceFilter withBoundedProperty( final String key )
        {
            properties.put( key, null );
            return this;
        }

        public CapabilityReferenceFilter withProperty( final String key, final String value )
        {
            properties.put( key, value );
            return this;
        }

        public CapabilityReferenceFilter onRepository( final String key, final String repositoryId )
        {
            return withProperty( key, "repo_" + repositoryId );
        }

        public CapabilityReferenceFilter onGroup( final String key, final String groupId )
        {
            return withProperty( key, "group_" + groupId );
        }

        @Override
        public boolean apply( @Nullable final CapabilityReference input )
        {
            if ( input == null )
            {
                return false;
            }
            if ( typeId != null && !typeId.equals( input.type().toString() ) )
            {
                return false;
            }
            if ( properties != null && !properties.isEmpty() )
            {
                final Map<String, String> inputPropertiesMap = input.properties();
                if ( inputPropertiesMap == null || inputPropertiesMap.isEmpty() )
                {
                    return false;
                }

                for ( Map.Entry<String, String> entry : properties.entrySet() )
                {
                    final String key = entry.getKey();
                    if ( key == null )
                    {
                        return false;
                    }
                    final String value = entry.getValue();
                    if ( value == null )
                    {
                        if ( !inputPropertiesMap.containsKey( key ) )
                        {
                            return false;
                        }
                    }
                    else
                    {
                        final String inputValue = inputPropertiesMap.get( key );
                        if ( inputValue == null || !value.equals( inputValue ) )
                        {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

    }

}
