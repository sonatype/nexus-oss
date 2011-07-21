package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;

import com.thoughtworks.xstream.XStream;

public class ITHelperLogUtils
{

    private static final String BASE_URI = "service/local/loghelper";
    
    private static XStream xstream;

    static
    {
        xstream = XStreamFactory.getXmlXStream();
    }

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
