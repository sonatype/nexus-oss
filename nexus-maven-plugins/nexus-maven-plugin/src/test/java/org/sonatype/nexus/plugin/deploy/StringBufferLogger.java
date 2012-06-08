/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.deploy;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public class StringBufferLogger
    extends AbstractLogger
{
    private final StringBuilder stringBuilder = new StringBuilder();

    public String getLoggedStuff()
    {
        return stringBuilder.toString();
    }

    public StringBufferLogger( final String name )
    {
        super( LEVEL_INFO, name );
    }

    @Override
    public void warn( String message, Throwable throwable )
    {
        stringBuilder.append( message ).append( "\n" );
    }

    @Override
    public void info( String message, Throwable throwable )
    {
        stringBuilder.append( message ).append( "\n" );
    }

    @Override
    public void fatalError( String message, Throwable throwable )
    {
        stringBuilder.append( message ).append( "\n" );
    }

    @Override
    public void error( String message, Throwable throwable )
    {
        stringBuilder.append( message ).append( "\n" );
    }

    @Override
    public void debug( String message, Throwable throwable )
    {
        stringBuilder.append( message ).append( "\n" );
    }

    @Override
    public Logger getChildLogger( String name )
    {
        return this;
    }
}
