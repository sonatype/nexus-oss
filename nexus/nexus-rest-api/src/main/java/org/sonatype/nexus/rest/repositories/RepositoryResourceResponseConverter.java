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
