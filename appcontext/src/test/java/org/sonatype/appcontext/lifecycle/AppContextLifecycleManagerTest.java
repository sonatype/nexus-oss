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
