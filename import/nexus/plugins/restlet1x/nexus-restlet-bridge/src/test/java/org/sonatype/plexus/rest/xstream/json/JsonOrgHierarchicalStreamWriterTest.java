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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;

import com.thoughtworks.xstream.XStream;

public class JsonOrgHierarchicalStreamWriterTest
    extends PlexusTestCase
{

    protected XStream xstream;

    protected void prepare()
    {
        this.xstream = new XStream( new JsonOrgHierarchicalStreamDriver() );

        this.xstream.registerConverter( new PrimitiveKeyedMapConverter( xstream.getMapper() ) );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        prepare();
    }

    protected void serialize( Object o )
        throws IOException
    {
        String result = xstream.toXML( o );
        
        System.out.println( result );
    }

    public void testList()
        throws Exception
    {
        System.out.println( " == LISTS ==" );

        List<String> strings = new ArrayList<String>( 3 );
        strings.add( "oneList" );
        strings.add( "twoList" );
        strings.add( "threeList" );
        serialize( strings );
    }

    public void testMap()
        throws Exception
    {
        System.out.println( " == MAP ==" );

        HashMap map = new HashMap();
        map.put( "key", "value" );
        map.put( "aNumber", 1975 );
        map.put( "aBoolean", Boolean.TRUE );
        map.put( "one-nine-seven-five", 1975 );
        serialize( map );

        HashMap complicated = new HashMap();
        complicated.put( "simpleKey", "someValue" );
        complicated.put( "aMap", map );
        Object[] objects = new Object[3];
        objects[0] = "text";
        objects[1] = 1975;
        objects[2] = Boolean.TRUE;
        complicated.put( "arrayOfObjects", objects );
        serialize( complicated );
    }

    public void testArray()
        throws Exception
    {
        System.out.println( " == ARRAYS ==" );

        String[] strings = new String[2];
        strings[0] = "one";
        strings[1] = "two";
        serialize( strings );

        int[] ints = new int[3];
        ints[0] = 1;
        ints[1] = 2;
        ints[2] = 3;
        serialize( ints );

        Object[] objects = new Object[3];
        objects[0] = "text";
        objects[1] = 1975;
        objects[2] = Boolean.TRUE;
        serialize( objects );
    }

    public void testCustomObjects()
        throws Exception
    {
        System.out.println( " == CUSTOM OBJECTS ==" );

        OneValued ovn = new OneValued();
        ovn.stringValue = null;
        serialize( ovn );

        OneValued ov = new OneValued();
        ov.stringValue = "some string value";
        serialize( ov );

        ThreeValued tw = new ThreeValued();
        tw.stringValue = "again some string field";
        tw.intValue = 1975;
        tw.boolValue = true;
        serialize( tw );

        CombinedValued co = new CombinedValued();
        co.stringValue = "custom object";
        int[] ints = new int[3];
        ints[0] = 1;
        ints[1] = 2;
        ints[2] = 3;
        co.ints = ints;

        List<String> strings = new ArrayList<String>( 3 );
        strings.add( "oneList" );
        strings.add( "twoList" );
        strings.add( "threeList" );
        co.objectsList = strings;

        HashMap map = new HashMap();
        map.put( "key", "value" );
        map.put( "aNumber", 1975 );
        co.objectMap = map;

        serialize( co );
    }

}
