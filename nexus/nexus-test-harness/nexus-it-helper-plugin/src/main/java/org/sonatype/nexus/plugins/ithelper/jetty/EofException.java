package org.sonatype.nexus.plugins.ithelper.jetty;

public class EofException
    extends Exception
{

    private static final long serialVersionUID = -5731938195527790534L;

    public EofException( String message )
    {
        super( message );
    }

}
