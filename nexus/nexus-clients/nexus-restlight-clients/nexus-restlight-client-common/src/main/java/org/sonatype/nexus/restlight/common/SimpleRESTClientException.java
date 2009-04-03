package org.sonatype.nexus.restlight.common;

public class SimpleRESTClientException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    public SimpleRESTClientException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public SimpleRESTClientException( String message )
    {
        super( message );
    }

}
