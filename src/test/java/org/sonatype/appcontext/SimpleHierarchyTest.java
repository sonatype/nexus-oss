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
