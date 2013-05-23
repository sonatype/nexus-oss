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

public class TaskUtil
{
    private static final ThreadLocal<ProgressListener> CURRENT = new ThreadLocal<ProgressListener>()
    {
        protected ProgressListener initialValue()
        {
            return new ProgressListenerWrapper( null );
        }
    };

    protected static void setCurrent( final ProgressListener progressListener )
    {
        if ( progressListener != null )
        {
            CURRENT.set( new CancellableProgressListenerWrapper( progressListener ) );
        }
        else
        {
            CURRENT.set( new ProgressListenerWrapper( null ) );
        }
    }

    /**
     * Returns current {@link ProgressListener} instance, never returns null.
     * 
     * @return
     */
    public static ProgressListener getCurrentProgressListener()
    {
        return CURRENT.get();
    }

    /**
     * Checks for user cancellation or thread interruption. In any of those both cases, {@link TaskInterruptedException}
     * is thrown that might be caught and handled by caller. If not handled, thread will die-off. If handled, caller
     * must ensure and handle interrupt flag of current thread.
     */
    public static void checkInterruption()
        throws TaskInterruptedException
    {
        Thread.yield();

        if ( getCurrentProgressListener().isCanceled() )
        {
            throw new TaskInterruptedException( "Thread \"" + Thread.currentThread().getName() + "\" is canceled!",
                true );
        }

        if ( Thread.interrupted() )
        {
            throw new TaskInterruptedException( "Thread \"" + Thread.currentThread().getName() + "\" is interrupted!",
                false );
        }
    }
}
