/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty.thread;

/**
 * A simple wrapper that simply "detects" did a wrapped runnable "died" or exited cleanly. Once death detected, the flag
 * remains set.
 * 
 * @author cstamas
 * @since 1.3
 */
public class RunnableWrapper
    implements Runnable
{
    private final Runnable runnable;

    static boolean unexpectedThrowable = false;

    public RunnableWrapper( final Runnable runnable )
    {
        this.runnable = runnable;
    }

    public void run()
    {
        try
        {
            runnable.run();
        }
        catch ( Throwable e )
        {
            unexpectedThrowable = true;
        }
    }
}
