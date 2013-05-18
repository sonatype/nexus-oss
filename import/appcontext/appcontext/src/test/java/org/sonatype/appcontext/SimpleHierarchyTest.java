/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.appcontext;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sonatype.appcontext.internal.ContextStringDumper;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;

public class SimpleHierarchyTest
    extends TestCase
{
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // Set this to have it "catched"
        System.setProperty( "default.blah", "default!" );
        System.setProperty( "child.blah", "child!" );
        System.setProperty( "grandchild.blah", "grandchild!" );
    }

    protected void tearDown()
        throws Exception
    {
        System.clearProperty( "default.blah" );
        System.clearProperty( "child.blah" );
        System.clearProperty( "grandchild.blah" );

        super.tearDown();
    }

    protected AppContext create( final String id, final File propertiesFile, final AppContext parent )
        throws AppContextException
    {
        AppContextRequest request = Factory.getDefaultRequest( id, parent );
        request.getSources().add( new PropertiesFileEntrySource( propertiesFile ) );
        return Factory.create( request );
    }

    public void testC02Hierarchy()
        throws Exception
    {
        final AppContext def = create( "default", new File( "src/test/resources/c02/default.properties" ), null );
        final AppContext child = create( "child", new File( "src/test/resources/c02/child.properties" ), def );
        final AppContext grandchild =
            create( "grandchild", new File( "src/test/resources/c02/grandchild.properties" ), child );

        // "oldvalue" is inherited and still here!
        Assert.assertEquals( "oldvalue", grandchild.get( "oldvalue" ) );

        // but, "grandchild" listens new music
        Assert.assertEquals( "dj Palotai", grandchild.get( "music" ) );
    }

    public void testC02Dump()
        throws Exception
    {
        final AppContext def = create( "default", new File( "src/test/resources/c02/default.properties" ), null );
        final AppContext child = create( "child", new File( "src/test/resources/c02/child.properties" ), def );
        final AppContext grandchild =
            create( "grandchild", new File( "src/test/resources/c02/grandchild.properties" ), child );

        grandchild.put( "wowThisIsAnObject", new Object() );

        System.out.println( " *** " );
        System.out.println( grandchild.get( "basedir" ) );
        System.out.println( " *** " );

        System.out.println( ContextStringDumper.dumpToString( grandchild ) );
    }

}
