/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.plexus.rest.xstream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This Converter allows changing the alias of an element in a list. 
 * <p>
 * Usage: 
 * <p><code>
 * &nbsp;&nbsp;&nbsp;xstream.registerLocalConverter( &lt;class containing list&gt;, "listOfStrings", new AliasingListConverter( String.class, "value"));
 * </code>
 * <p>
 * NOTE: only tested with lists of Strings.
 * 
 */
public class AliasingListConverter
    implements Converter
{

    /**
     * The type of object list is expected to convert.
     */
    private Class<?> type;

    /**
     * 
     */
    private String alias;
    
    public AliasingListConverter( Class<?> type, String alias )
    {
        this.type = type;
        this.alias = alias;
    }

    /* (non-Javadoc)
     * @see com.thoughtworks.xstream.converters.ConverterMatcher#canConvert(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public boolean canConvert( Class type )
    {
        return List.class.isAssignableFrom( type );
    }

    /* (non-Javadoc)
     * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
     */
    public void marshal( Object source, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        List<?> list = (List<?>) source;
        for ( Iterator<?> iter = list.iterator(); iter.hasNext(); )
        {
            Object elem = iter.next();
            if ( !elem.getClass().isAssignableFrom( type ) )
            {
                throw new ConversionException( "Found "+elem.getClass() +", expected to find: "+ this.type +" in List." );
            }
            
            ExtendedHierarchicalStreamWriterHelper.startNode(writer, alias, elem.getClass());
            context.convertAnother(elem);
            writer.endNode();
        }
    }

    /* (non-Javadoc)
     * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
     */
    @SuppressWarnings( "unchecked" )
    public Object unmarshal( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        List list = new ArrayList();
        while ( reader.hasMoreChildren() )
        {
            reader.moveDown();
            list.add( context.convertAnother( list, type ) );
            reader.moveUp();
        }
        return list;
    }
}
