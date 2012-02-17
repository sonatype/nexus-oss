package org.sonatype.appcontext;

/**
 * Thrown when some fatal exception happens during interpolation, like cycle detected in expressions.
 * 
 * @author cstamas
 */
public class AppContextInterpolationException
    extends AppContextException
{
    private static final long serialVersionUID = 7958491320532121743L;

    public AppContextInterpolationException( String message )
    {
        super( message );
    }

    public AppContextInterpolationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
