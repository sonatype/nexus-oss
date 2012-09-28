package org.sonatype.appcontext.lifecycle;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;

public class AppContextLifecycleManagerTest
    extends TestCase
{
    private int stoppedInvoked = 0;

    private int lifecycleHandlerInvoked = 0;

    public void testSimple()
    {
        final AppContextRequest request = Factory.getDefaultRequest( "c01" );
        final AppContext context = Factory.create( request );

        context.getLifecycleManager().registerManaged( new Stoppable()
        {
            public void handle()
            {
                stoppedInvoked++;
            }
        } );
        context.getLifecycleManager().registerManaged( new LifecycleHandler()
        {
            public void handle()
            {
                lifecycleHandlerInvoked++;
            }
        } );

        // call Stopped 1st
        context.getLifecycleManager().invokeHandler( Stoppable.class );
        // call LifecycleHandler (effectively calling all)
        context.getLifecycleManager().invokeHandler( LifecycleHandler.class );

        Assert.assertEquals( 2, stoppedInvoked );
        Assert.assertEquals( 1, lifecycleHandlerInvoked );
    }
}
