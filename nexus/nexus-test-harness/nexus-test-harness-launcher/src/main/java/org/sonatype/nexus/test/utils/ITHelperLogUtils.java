/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;

public class ITHelperLogUtils
{

    private static final String BASE_URI = "service/local/loghelper";

    public static void debug( String message )
        throws Exception
    {
        log( null, "DEBUG", message, null, null );
    }

    public static void debug( String loggerName, String message )
        throws Exception
    {
        log( loggerName, "DEBUG", message, null, null );
    }

    public static void debug( String message, Exception exception )
        throws Exception
    {
        log( null, "DEBUG", message, exception );
    }

    public static void error( String message )
        throws Exception
    {
        log( null, "ERROR", message, null, null );
    }

    public static void error( String loggerName, String message )
        throws Exception
    {
        log( loggerName, "ERROR", message, null, null );
    }

    public static void error( String message, Exception exception )
        throws Exception
    {
        log( null, "ERROR", message, exception );
    }

    public static void error( String message, String exceptionType, String exceptionMessage )
        throws Exception
    {
        log( null, "ERROR", message, exceptionType, exceptionMessage );
    }

    public static void warn( String message )
        throws Exception
    {
        log( null, "WARN", message, null, null );
    }

    public static void warn( String loggerName, String message )
        throws Exception
    {
        log( loggerName, "WARN", message, null, null );
    }

    public static void warn( String message, Exception exception )
        throws Exception
    {
        log( null, "WARN", message, exception );
    }

    public static void warn( String message, String exceptionType, String exceptionMessage )
        throws Exception
    {
        log( null, "WARN", message, exceptionType, exceptionMessage );
    }

    public static void log( String loggerName, String level, String message, Exception exception )
        throws Exception
    {
        log( loggerName, level, message, exception.getClass().getName(), exception.getMessage() );
    }

    public static void log( String loggerName, String level, String message, String exceptionType,
                            String exceptionMessage )
        throws Exception
    {
        String uri = "";
        if ( loggerName != null )
        {
            uri += "&loggerName=" + loggerName;
        }
        if ( level != null )
        {
            uri += "&level=" + level;
        }
        if ( message != null )
        {
            uri += "&message=" + message;
        }
        if ( exceptionType != null )
        {
            uri += "&exceptionType=" + exceptionType;
        }
        if ( exceptionMessage != null )
        {
            uri += "&exceptionMessage=" + exceptionMessage;
        }
        if ( !uri.equals( "" ) )
        {
            uri = uri.substring( 1 );
        }
        uri = BASE_URI + "?" + uri;

        final Status status = RequestFacade.doGetForStatus( uri );

        if ( !status.isSuccess() )
        {
            throw new IOException( "The loghelper REST resource reported an error (" + status.toString()
                + "), bailing out!" );
        }

    }

}
