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
