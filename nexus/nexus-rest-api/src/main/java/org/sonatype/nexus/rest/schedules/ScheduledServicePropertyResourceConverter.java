/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.schedules;

import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class ScheduledServicePropertyResourceConverter
    extends AbstractReflectionConverter
{
    public ScheduledServicePropertyResourceConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return ScheduledServicePropertyResource.class.equals( type );
    }
    
    public Object doUnmarshal( Object source, HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        ScheduledServicePropertyResource resource = (ScheduledServicePropertyResource) source;
        while ( reader.hasMoreChildren() )
        {
            reader.moveDown();
            if ( "id".equals( reader.getNodeName() ) )
            {
                resource.setId( (String) context.convertAnother( source, String.class ) );
            }
            else if ( "value".equals( reader.getNodeName() ) )
            {
                resource.setValue( (String) context.convertAnother( source, String.class ) );
            }
            reader.moveUp();
        }
        return resource;
    }
}
