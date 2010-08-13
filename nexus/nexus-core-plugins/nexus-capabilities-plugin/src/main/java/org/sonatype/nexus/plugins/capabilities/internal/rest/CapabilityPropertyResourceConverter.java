package org.sonatype.nexus.plugins.capabilities.internal.rest;

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class CapabilityPropertyResourceConverter
    extends AbstractReflectionConverter
{
    public CapabilityPropertyResourceConverter( final Mapper mapper, final ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( final Class type )
    {
        return CapabilityPropertyResource.class.equals( type );
    }

    @Override
    public Object doUnmarshal( final Object source, final HierarchicalStreamReader reader,
                               final UnmarshallingContext context )
    {
        final CapabilityPropertyResource resource = (CapabilityPropertyResource) source;
        while ( reader.hasMoreChildren() )
        {
            reader.moveDown();
            if ( "key".equals( reader.getNodeName() ) )
            {
                resource.setKey( (String) context.convertAnother( source, String.class ) );
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
