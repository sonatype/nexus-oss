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

public class ProgressListenerWrapper
    implements ProgressListener
{
    private final ProgressListener wrapped;

    public ProgressListenerWrapper( final ProgressListener wrapped )
    {
        this.wrapped = wrapped;
    }

    public void beginTask( final String name )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.beginTask( name );
        }
    }

    public void beginTask( final String name, final int toDo )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.beginTask( name, toDo );
        }
    }

    public void working( final int workDone )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( workDone );
        }
    }

    public void working( final String message )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( message );
        }
    }

    public void working( final String message, final int work )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.working( message, work );
        }
    }

    public void endTask( final String message )
    {
        TaskUtil.checkInterruption();

        if ( wrapped != null )
        {
            wrapped.endTask( message );
        }
    }

    public boolean isCanceled()
    {
        if ( wrapped != null )
        {
            return wrapped.isCanceled();
        }
        else
        {
            return false;
        }
    }

    public void cancel()
    {
        if ( wrapped != null )
        {
            wrapped.cancel();
        }
    }
}
