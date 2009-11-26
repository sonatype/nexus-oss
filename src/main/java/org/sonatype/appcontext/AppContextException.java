package org.sonatype.appcontext;

/**
 * Thrown when some fatal exception happens, that is probably not recoverable, but might be caused by wrong request. So,
 * caller might try again (ie. on user interaction or not).
 * 
 * @author cstamas
 */
public class AppContextException
    extends Exception
{
    private static final long serialVersionUID = 3396476391595403414L;

    public AppContextException( String message )
    {
        super( message );
    }

    public AppContextException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
