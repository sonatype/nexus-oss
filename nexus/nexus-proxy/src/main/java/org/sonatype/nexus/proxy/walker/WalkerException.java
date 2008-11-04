package org.sonatype.nexus.proxy.walker;

/**
 * Thrown by walker if something terrible happened.
 * 
 * @author cstamas
 */
public class WalkerException
    extends Exception
{
    private static final long serialVersionUID = 3197048259219625491L;

    private final WalkerContext walkerContext;

    public WalkerException( WalkerContext context, String message, Throwable cause )
    {
        super( message, cause );

        this.walkerContext = context;
    }

    public WalkerException( WalkerContext context, String message )
    {
        super( message );

        this.walkerContext = context;
    }

    public WalkerException( WalkerContext context, Throwable cause )
    {
        super( cause );

        this.walkerContext = context;
    }

    public WalkerContext getWalkerContext()
    {
        return walkerContext;
    }

}
