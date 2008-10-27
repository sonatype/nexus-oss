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
package org.sonatype.nexus.rest.repositories;

import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.plexus.rest.xstream.LookAheadStreamReader;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * XStream converter that helps XStream to convert incoming JSON data properly. It handles RepositoryBaseResource
 * classes only.
 * 
 * @author cstamas
 */
public class RepositoryBaseResourceConverter
    extends AbstractReflectionConverter
{

    public RepositoryBaseResourceConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return RepositoryBaseResource.class.equals( type );
    }

    protected Object instantiateNewInstance( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() )
            || LookAheadStreamReader.class.isAssignableFrom( reader.underlyingReader().getClass() ) )
        {
            String repoType = null;

            if ( LookAheadStreamReader.class.isAssignableFrom( reader.getClass() ) )
            {
                repoType = ( (LookAheadStreamReader) reader ).getFieldValue( "repoType" );
            }
            else
            {
                repoType = ( (LookAheadStreamReader) reader.underlyingReader() ).getFieldValue( "repoType" );
            }

            if ( repoType == null )
            {
                return super.instantiateNewInstance( reader, context );
            }
            else if ( AbstractRepositoryPlexusResource.REPO_TYPE_VIRTUAL.equals( repoType ) )
            {
                return new RepositoryShadowResource();
            }
            else if (AbstractRepositoryPlexusResource.REPO_TYPE_PROXIED.equals( repoType ) )
            {
                return new RepositoryProxyResource();
            }
            else
            {
                return new RepositoryResource();
            }
        }
        else
        {
            return super.instantiateNewInstance( reader, context );
        }
    }
}
