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

/**
 * Runtime exception thrown in cases when thread is interrupted. Semantical meaning is almost same as
 * {@link InterruptedException} except this one is unchecked exception.
 * 
 * @author cstamas
 */
public class TaskInterruptedException
    extends RuntimeException
{
    private static final long serialVersionUID = 5758132070000732555L;

    private final boolean cancelled;

    public TaskInterruptedException( String message, boolean cancelled )
    {
        super( message );

        this.cancelled = cancelled;
    }

    public TaskInterruptedException( String message, Throwable cause )
    {
        super( message, cause );

        this.cancelled = false;
    }

    public TaskInterruptedException( Throwable cause )
    {
        super( cause );

        this.cancelled = false;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }
}
