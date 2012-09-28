package org.sonatype.appcontext;

import java.util.Properties;

import junit.framework.TestCase;

import org.sonatype.appcontext.source.PropertiesEntrySource;

public class Nexus5279Test
    extends TestCase
{
    public void testNexus5279()
    {
        // create parent using two sources
        final AppContextRequest parentRequest = Factory.getDefaultRequest( "parent", null );
        parentRequest.getPublishers().clear();
        final Properties p1 = new Properties();
        p1.put( "foo", "bar" );
        parentRequest.getSources().add( new PropertiesEntrySource( "parent", p1 ) );
        final Properties p2 = new Properties();
        p2.put( "oof", "rab" );
        parentRequest.getSources().add( new PropertiesEntrySource( "parent-test", p2 ) );
        final AppContext parentContext = Factory.create( parentRequest );

        // check it's properties
        assertEquals( 2, parentContext.entrySet().size() );
        assertEquals( 2, parentContext.values().size() );
        assertEquals( "bar", parentContext.get( "foo" ) );
        assertEquals( "rab", parentContext.get( "oof" ) );

        // create empty child of the parent context
        final AppContextRequest childRequest = Factory.getDefaultRequest( "child", parentContext );
        childRequest.getPublishers().clear();
        final AppContext context2 = Factory.create( childRequest );

        // check child properties, key mappings are accessing (they come from parent)
        assertEquals( "bar", context2.get( "foo" ) );
        assertEquals( "rab", context2.get( "oof" ) );
        // while child is actually empty
        assertEquals( 0, context2.entrySet().size() );
        assertEquals( 0, context2.values().size() );
        // while flattened map of child has proper sizes
        assertEquals( 2, context2.flatten().entrySet().size() );
        assertEquals( 2, context2.flatten().values().size() );
    }
}
