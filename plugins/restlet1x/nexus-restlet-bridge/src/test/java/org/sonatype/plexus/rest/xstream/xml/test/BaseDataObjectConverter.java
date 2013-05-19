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
