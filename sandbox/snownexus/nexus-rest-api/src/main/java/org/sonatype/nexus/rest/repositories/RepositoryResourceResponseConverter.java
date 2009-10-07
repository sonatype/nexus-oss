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

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class RepositoryResourceResponseConverter
extends AbstractReflectionConverter
{

    public RepositoryResourceResponseConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return RepositoryResourceResponse.class.equals( type );
    }

    public void marshal( Object value, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        
        // removes the class="class.name" attribute
        RepositoryResourceResponse top = (RepositoryResourceResponse) value;
        if ( top.getData() != null )
        {
            // make sure the data's repoType field is valid, or we wont be able to deserialize it on the other side
            if( StringUtils.isEmpty( top.getData().getRepoType() ) )
            {
                throw new ConversionException( "Missing value for field: RepositoryResourceResponse.data.repoType." );
            }
            
            writer.startNode( "data" );
            context.convertAnother( top.getData() );
            writer.endNode();
        }

    }
}
