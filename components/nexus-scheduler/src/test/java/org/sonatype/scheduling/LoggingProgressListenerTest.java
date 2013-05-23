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
package org.sonatype.scheduling;

import org.junit.Test;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * Tests for {@link LoggingProgressListener}.
 */
public class LoggingProgressListenerTest
    extends TestSupport
{
    @Test
    public void testSimple()
    {
        LoggingProgressListener pl = new LoggingProgressListener( "foo" );

        pl.beginTask( "Task1", 10 );

        pl.working( 3 );
        
        pl.working( "Hm, this is hard!", 3 );

        pl.beginTask( "Task2", 10 );

        pl.working( 3 );

        pl.beginTask( "Task3", 10 );

        pl.working( 3 );
        pl.working( "Hm, this is hard!", 5 );

        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
    }

    @Test
    public void testSimpleUnknown()
    {
        LoggingProgressListener pl = new LoggingProgressListener( "foo" );

        pl.beginTask( "Task1" );

        pl.working( 3 );
        
        pl.working( "Hm, this is hard!", 3 );

        pl.beginTask( "Task2", 10 );

        pl.working( 3 );

        pl.beginTask( "Task3", 10 );

        pl.working( 3 );
        pl.working( "Hm, this is hard!", 5 );

        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
        pl.endTask( "Okay!" );
    }

}
