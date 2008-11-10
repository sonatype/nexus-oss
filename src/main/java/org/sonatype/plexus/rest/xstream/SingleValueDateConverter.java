package org.sonatype.plexus.rest.xstream;

import java.util.Date;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SingleValueDateConverter
    implements Converter
{
    public void marshal( Object source, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        Date date = (Date) source;
        writer.setValue( String.valueOf( date.getTime() ) );
    }

    public Object unmarshal( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        return new Date( Long.parseLong( reader.getValue() ) );
    }

    public boolean canConvert( Class type )
    {
        return type.equals( Long.class ) 
            || type.equals( Integer.class ) 
            || type.equals( Date.class ) 
            || type.equals( String.class );
    }

}
