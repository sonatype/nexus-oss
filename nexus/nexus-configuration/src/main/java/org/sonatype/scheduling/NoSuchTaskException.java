package org.sonatype.scheduling;

public class NoSuchTaskException
    extends Exception
{
    private static final long serialVersionUID = 9212575645497920481L;

    public NoSuchTaskException( String id )
    {
        super( "There is no running/active task with ID=" + id );
    }

    public NoSuchTaskException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
