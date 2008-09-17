package org.sonatype.plexus.rest.xstream.xml.test;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class TopLevelObjectConverter
    extends AbstractReflectionConverter
{

    public TopLevelObjectConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return TopLevelObject.class.equals( type );
    }

    public void marshal( Object value, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        
        // removes the class="class.name" attribute
        
        TopLevelObject top = (TopLevelObject) value;
        if ( top.getData() != null )
        {
            writer.startNode( "data" );
            context.convertAnother( top.getData() );
            writer.endNode();
        }

    }

}
