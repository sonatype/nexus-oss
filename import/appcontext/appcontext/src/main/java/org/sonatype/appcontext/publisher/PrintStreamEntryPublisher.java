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
package org.sonatype.appcontext.publisher;

import java.io.PrintStream;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A EntryPublisher the publishes the contexts to supplied {@code java.io.PrintStream}, or to {@code System.out}.
 * 
 * @author cstamas
 */
public class PrintStreamEntryPublisher
    extends AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
    /**
     * The PrintStream to be used for publishing.
     */
    private final PrintStream printStream;

    /**
     * Constructs publisher the publishes to {@code System.out}.
     */
    public PrintStreamEntryPublisher()
    {
        this( System.out );
    }

    /**
     * Constructs publisher to use supplied print stream.
     * 
     * @param printStream
     * @throws NullPointerException if {@code preintStream} is null
     */
    public PrintStreamEntryPublisher( final PrintStream printStream )
    {
        this.printStream = Preconditions.checkNotNull( printStream );
    }

    public void publishEntries( final AppContext context )
    {
        printStream.println( getDumpAsString( context ) );
    }
}
