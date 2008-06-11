package org.sonatype.nexus.rest.schedules;

import java.lang.reflect.Field;

import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
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
    
    protected void doMarshal( Object source, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        ScheduledServicePropertyResource resource = (ScheduledServicePropertyResource) source;
        if ( resource.getId() != null )
        {
            writer.startNode( "id" );
            context.convertAnother( resource.getId() );
            writer.endNode();
        }
        if ( resource.getValue() != null )
        {
            writer.startNode( "value" );
            context.convertAnother( resource.getValue() );
            writer.endNode();
        }
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
