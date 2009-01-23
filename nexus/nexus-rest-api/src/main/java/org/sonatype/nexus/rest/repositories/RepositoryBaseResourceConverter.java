/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
