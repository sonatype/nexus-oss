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
package org.sonatype.nexus.rest.privileges;

import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.plexus.rest.xstream.json.LookAheadStreamReader;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class PrivilegeBaseStatusResourceConverter
    extends AbstractReflectionConverter
{
    public PrivilegeBaseStatusResourceConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return PrivilegeBaseStatusResource.class.equals( type );
    }
    
    protected Object instantiateNewInstance( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() )
                        || LookAheadStreamReader.class.isAssignableFrom( reader.underlyingReader().getClass() ) )
        {
            String type = null;

            if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() ) )
            {
                type = ( (LookAheadStreamReader) reader ).getFieldValue( "type" );
            }
            else
            {
                type = ( (LookAheadStreamReader) reader.underlyingReader() ).getFieldValue( "type" );
            }

            if ( type == null )
            {
                return super.instantiateNewInstance( reader, context );
            }
            else if ( AbstractPrivilegeResourceHandler.TYPE_REPO_TARGET.equals( type ))
            {
                return new PrivilegeTargetStatusResource();
            }
            else if ( AbstractPrivilegeResourceHandler.TYPE_APPLICATION.equals( type ))
            {
                return new PrivilegeApplicationStatusResource();
            }
            else
            {
                return new PrivilegeBaseStatusResource();
            }
        }
        else
        {
            return super.instantiateNewInstance( reader, context );
        }
    }
}
