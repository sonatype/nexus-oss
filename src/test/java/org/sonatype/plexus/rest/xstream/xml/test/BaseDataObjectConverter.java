package org.sonatype.plexus.rest.xstream.xml.test;

import java.io.IOException;

import org.sonatype.plexus.rest.xstream.LookAheadStreamReader;
import org.xmlpull.v1.XmlPullParserException;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class BaseDataObjectConverter
    extends AbstractReflectionConverter
{

    public BaseDataObjectConverter( Mapper mapper, ReflectionProvider reflectionProvider )
    {
        super( mapper, reflectionProvider );
    }

    public boolean canConvert( Class type )
    {
        return BaseDataObject.class.equals( type );
    }

    protected Object instantiateNewInstance( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        BaseDataObject data = null;

        reader = reader.underlyingReader();
        LookAheadStreamReader xppReader = null;

        if ( reader instanceof LookAheadStreamReader )
        {
            xppReader = (LookAheadStreamReader) reader;
        }
        else
        {
            throw new RuntimeException( "reader: " + reader.getClass() );
        }

        String type = xppReader.getFieldValue( "type" );

        if ( "type-one".equals( type ) )
        {
            data = new DataObject1();
        }
        else if ( "type-two".equals( type ) )
        {
            data = new DataObject2();
        }

        return data;
    }

}
