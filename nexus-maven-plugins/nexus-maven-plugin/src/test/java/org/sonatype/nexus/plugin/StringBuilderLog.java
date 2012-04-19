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
package org.sonatype.nexus.plugin;

import org.apache.maven.plugin.logging.Log;

public class StringBuilderLog
    implements Log
{
    private final StringBuilder stringBuilder;

    private Log log;

    public StringBuilderLog( final Log log )
    {
        this.stringBuilder = new StringBuilder();
        this.log = log;
    }

    public String getLoggedOutput()
    {
        return stringBuilder.toString();
    }

    public boolean isDebugEnabled()
    {
        return log.isDebugEnabled();
    }

    public void debug( CharSequence content )
    {
        stringBuilder.append( content ).append( "\n" );
        log.debug( content );
    }

    public void debug( CharSequence content, Throwable error )
    {
        stringBuilder.append( content ).append( "\n" );
        log.debug( content, error );
    }

    public void debug( Throwable error )
    {
        log.debug( error );
    }

    public boolean isInfoEnabled()
    {
        return log.isInfoEnabled();
    }

    public void info( CharSequence content )
    {
        stringBuilder.append( content ).append( "\n" );
        log.info( content );
    }

    public void info( CharSequence content, Throwable error )
    {
        stringBuilder.append( content ).append( "\n" );
   log.info( content, error );
    }

    public void info( Throwable error )
    {
        log.info( error );
    }

    public boolean isWarnEnabled()
    {
        return log.isWarnEnabled();
    }

    public void warn( CharSequence content )
    {
        stringBuilder.append( content ).append( "\n" );
        log.warn( content );
    }

    public void warn( CharSequence content, Throwable error )
    {
        stringBuilder.append( content ).append( "\n" );
        log.warn( content, error );
    }

    public void warn( Throwable error )
    {
        log.warn( error );
    }

    public boolean isErrorEnabled()
    {
        return log.isErrorEnabled();
    }

    public void error( CharSequence content )
    {
        stringBuilder.append( content ).append( "\n" );
        log.error( content );
    }

    public void error( CharSequence content, Throwable error )
    {
        stringBuilder.append( content ).append( "\n" );
        log.error( content, error );
    }

    public void error( Throwable error )
    {
        log.error( error );
    }
}
