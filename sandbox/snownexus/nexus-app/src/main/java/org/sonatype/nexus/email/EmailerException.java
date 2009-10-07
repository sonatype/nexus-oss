package org.sonatype.nexus.email;

public class EmailerException
    extends Exception
{

    private static final long serialVersionUID = -8229443120962556912L;

    public EmailerException()
    {
        this(null);
    }

    public EmailerException( String message )
    {
        this( message, null );
    }

    public EmailerException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
