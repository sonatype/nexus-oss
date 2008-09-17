package org.sonatype.nexus.rest.privileges;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class PrivilegeStatusResourceResponseConverter
extends AbstractReflectionConverter
{

    public PrivilegeStatusResourceResponseConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return PrivilegeStatusResourceResponse.class.equals( type );
    }

    public void marshal( Object value, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        
        // removes the class="class.name" attribute
        PrivilegeStatusResourceResponse top = (PrivilegeStatusResourceResponse) value;
        if ( top.getData() != null )
        {
            // make sure the data's repoType field is valid, or we wont be able to deserialize it on the other side
            if( StringUtils.isEmpty( top.getData().getType() ) )
            {
                throw new ConversionException( "Missing value for field: PrivilegeStatusResourceResponse.data.type." );
            }            
            writer.startNode( "data" );
            context.convertAnother( top.getData() );
            writer.endNode();
        }

    }
}
