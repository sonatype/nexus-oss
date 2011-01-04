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
package org.sonatype.nexus.rest.repositories;

import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
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

            if ( AbstractRepositoryPlexusResource.REPO_TYPE_VIRTUAL.equals( repoType ) )
            {
                return new RepositoryShadowResource();
            }
            else if ( AbstractRepositoryPlexusResource.REPO_TYPE_PROXIED.equals( repoType ) )
            {
                return new RepositoryProxyResource();
            }
            else if ( AbstractRepositoryPlexusResource.REPO_TYPE_HOSTED.equals( repoType ) )
            {
                return new RepositoryResource();
            }
            else if ( AbstractRepositoryPlexusResource.REPO_TYPE_GROUP.equals( repoType ) )
            {
                return new RepositoryGroupResource();
            }
            else
            {
                return super.instantiateNewInstance( reader, context );
            }
        }
        else
        {
            return super.instantiateNewInstance( reader, context );
        }
    }
}
