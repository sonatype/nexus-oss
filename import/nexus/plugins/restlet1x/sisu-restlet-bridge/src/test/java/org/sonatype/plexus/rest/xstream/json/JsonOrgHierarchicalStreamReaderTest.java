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

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;

import com.thoughtworks.xstream.XStream;

public class JsonOrgHierarchicalStreamReaderTest
    extends PlexusTestCase
{

    protected XStream xstream;

    protected void prepare()
        throws IOException
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

    protected Object deserialize( String o )
        throws IOException
    {
        return xstream.fromXML( o );
    }

    protected void deserialize( String o, Object root )
        throws IOException
    {
        xstream.fromXML( o, root );
    }
    
    public void testStringBoolean()
        throws IOException
    {
        OneValued one = new OneValued();
        deserialize( "{ \"org.sonatype.plexus.rest.xstream.json.OneValued\" : { \"stringValue\" : \"true\" }}", one );
        assertEquals( "true", one.stringValue );
    }

    public void testSimple()
        throws IOException
    {
        OneValued one = new OneValued();
        deserialize( "{ \"org.sonatype.plexus.rest.xstream.json.OneValued\" : { \"stringValue\" : \"something\" }}", one );
        assertEquals( "something", one.stringValue );

        ThreeValued three = new ThreeValued();
        deserialize(
            "{ \"org.sonatype.plexus.rest.xstream.json.OneValued\" : { \"stringValue\" : \"something\", \"boolValue\" : true, \"intValue\" : 1975 }}",
            three );
        assertEquals( "something", three.stringValue );
        assertEquals( true, three.boolValue );
        assertEquals( 1975, three.intValue );

    }

    public void testCombined()
        throws IOException
    {
        CombinedValued co = new CombinedValued();
        deserialize(
            "{ \"org.sonatype.plexus.rest.xstream.json.CombinedValued\" : {\"stringValue\":\"custom object\",\"ints\":[1,2,3],\"objectsList\":[\"oneList\",\"twoList\",\"threeList\"],\"objectMap\":{\"key\":\"value\",\"aNumber\":1975}}}",
            co );
        // deserialize(
        // "{ \"org.sonatype.plexus.rest.channel.json.CombinedValued\" : {\"stringValue\":\"custom
        // object\",\"ints\":[1,2,3],\"objectsList\":[\"oneList\",\"twoList\",\"threeList\"],\"objectMap\":{\"entry\":{\"string\":\"key\",\"string\":\"value\"},\"entry\":{\"string\":\"aNumber\",\"int\":1975}}}}",
        // co );

        assertEquals( "custom object", co.stringValue );
        assertEquals( 3, co.ints.length );
        assertEquals( 1, co.ints[0] );
        assertEquals( 2, co.ints[1] );
        assertEquals( 3, co.ints[2] );
        assertEquals( 3, co.objectsList.size() );
        assertEquals( "oneList", co.objectsList.get( 0 ) );
        assertEquals( "twoList", co.objectsList.get( 1 ) );
        assertEquals( "threeList", co.objectsList.get( 2 ) );

        // XXX: we have a bug here
        // we should avoid Maps serialized like in second example in JSON (using xstream default converter)!
        // XXX maps works only with PrimitiveKeyedMapConverter registered with XStream
        // XXX test will fail if using default XStream converter!
        assertEquals( 2, co.objectMap.size() );
        assertEquals( "value", co.objectMap.get( "key" ) );
        assertEquals( 1975, co.objectMap.get( "aNumber" ) );
    }

    public void testCombinedWithNullValues()
        throws IOException
    {
        CombinedValued co = new CombinedValued();
        deserialize(
            "{ \"org.sonatype.plexus.rest.xstream.json.CombinedValued\" : {\"stringValue\":null,\"ints\":[1,2,3],\"objectsList\":[\"oneList\",\"twoList\",\"threeList\"]}}",
            co );

        // string value should be null
        assertEquals( null, co.stringValue );
        assertEquals( 3, co.ints.length );
        assertEquals( 1, co.ints[0] );
        assertEquals( 2, co.ints[1] );
        assertEquals( 3, co.ints[2] );
        assertEquals( 3, co.objectsList.size() );
        assertEquals( "oneList", co.objectsList.get( 0 ) );
        assertEquals( "twoList", co.objectsList.get( 1 ) );
        assertEquals( "threeList", co.objectsList.get( 2 ) );

        // XXX: we have a bug here
        // we should avoid Maps serialized like in second example in JSON (using xstream default converter)!
        // XXX maps works only with PrimitiveKeyedMapConverter registered with XStream
        // XXX test will fail if using default XStream converter!
        assertEquals( null, co.objectMap );

        co = new CombinedValued();
        deserialize(
            "{ \"org.sonatype.plexus.rest.xstream.json.CombinedValued\" : {\"stringValue\":\"hyy\",\"ints\":[],\"objectsList\":[null,\"twoList\",\"threeList\"]}}",
            co );

        // string value should be null
        assertEquals( "hyy", co.stringValue );
        assertEquals( 0, co.ints.length );
        assertEquals( 3, co.objectsList.size() );
        assertEquals( null, co.objectsList.get( 0 ) );
        assertEquals( "twoList", co.objectsList.get( 1 ) );
        assertEquals( "threeList", co.objectsList.get( 2 ) );

        // XXX: we have a bug here
        // we should avoid Maps serialized like in second example in JSON (using xstream default converter)!
        // XXX maps works only with PrimitiveKeyedMapConverter registered with XStream
        // XXX test will fail if using default XStream converter!
        assertEquals( null, co.objectMap );

        co = new CombinedValued();
        deserialize(
            "{ \"org.sonatype.plexus.rest.xstream.json.CombinedValued\" : {\"stringValue\":null,\"ints\":[],\"objectsList\":[\"oneList\",\"twoList\",\"threeList\"]}}",
            co );

        // string value should be null
        assertEquals( null, co.stringValue );
        assertEquals( 0, co.ints.length );
        assertEquals( 3, co.objectsList.size() );
        assertEquals( "oneList", co.objectsList.get( 0 ) );
        assertEquals( "twoList", co.objectsList.get( 1 ) );
        assertEquals( "threeList", co.objectsList.get( 2 ) );

        // XXX: we have a bug here
        // we should avoid Maps serialized like in second example in JSON (using xstream default converter)!
        // XXX maps works only with PrimitiveKeyedMapConverter registered with XStream
        // XXX test will fail if using default XStream converter!
        assertEquals( null, co.objectMap );
    }

}
