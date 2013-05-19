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
package org.sonatype.plexus.rest.xstream.json;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.core.util.Primitives;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Handy converter for creating ad hoc JSON objects. This converter converts a Maps with keys (as String) as properties
 * and values (as Objects) to a values. Usable in reading and in writing. Warning! This converter will alter the objects
 * structure in XStream output, and will be deserializable only with using this same converter. Note: this is somehow
 * JSON specific, altough it works with XML pretty well to.
 * 
 * @author cstamas
 */
public class PrimitiveKeyedMapConverter
    extends AbstractCollectionConverter
{
    public PrimitiveKeyedMapConverter( Mapper mapper )
    {
        super( mapper );
    }

    public boolean canConvert( Class type )
    {
        return type.equals( HashMap.class ) || type.equals( Hashtable.class )
            || type.getName().equals( "java.util.LinkedHashMap" ) || type.getName().equals( "sun.font.AttributeMap" );
    }

    public void marshal( Object source, HierarchicalStreamWriter writer, MarshallingContext context )
    {
        Map map = (Map) source;
        for ( Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) iterator.next();

            Class pClass = Primitives.unbox( entry.getKey().getClass() );
            if ( String.class.equals( entry.getKey().getClass() ) || entry.getKey().getClass().isPrimitive()
                || ( pClass != null && pClass.isPrimitive() ) )
            {
                ExtendedHierarchicalStreamWriterHelper.startNode( writer, entry.getKey().toString(), entry
                    .getValue().getClass() );
            }
            else
            {
                throw new IllegalArgumentException( "Cannot convert maps with non-String keys!" );

            }
            context.convertAnother( entry.getValue() );
            writer.endNode();
        }
    }

    public Object unmarshal( HierarchicalStreamReader reader, UnmarshallingContext context )
    {
        Map map = (Map) createCollection( context.getRequiredType() );
        populateMap( reader, context, map );
        return map;
    }

    protected void populateMap( HierarchicalStreamReader reader, UnmarshallingContext context, Map map )
    {
        while ( reader.hasMoreChildren() )
        {
            reader.moveDown();
            String key = reader.getNodeName();
            Object value;
            if ( reader.hasMoreChildren() )
            {
                // map value is object
                // XXX not working in this way!
                value = readItem( reader, context, map );
            }
            else
            {
                String classAttribute = reader.getAttribute( mapper().aliasForAttribute( "class" ) );
                Class type;
                if ( classAttribute == null )
                {
                    type = mapper().realClass( key );
                }
                else
                {
                    type = mapper().realClass( classAttribute );
                }
                value = context.convertAnother( reader.getValue(), type );
            }
            map.put( key, value );
            reader.moveUp();
        }
    }
}
