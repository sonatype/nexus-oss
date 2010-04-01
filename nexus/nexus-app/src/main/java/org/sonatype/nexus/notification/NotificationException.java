package org.sonatype.nexus.notification;

import java.io.IOException;

@SuppressWarnings( "serial" )
public class NotificationException
    extends IOException
{

    public NotificationException( String s )
    {
        super( s );
    }

    public NotificationException( String s, Throwable cause )
    {
        super( s );

        this.initCause( cause );
    }

}
