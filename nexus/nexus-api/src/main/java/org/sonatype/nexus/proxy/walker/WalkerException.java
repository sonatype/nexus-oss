package org.sonatype.nexus.proxy.walker;

public class WalkerException
    extends RuntimeException
{
    private static final long serialVersionUID = 3471267154120984621L;

    private final WalkerContext walkerContext;

    public WalkerException( WalkerContext walkerContext, String message )
    {
        super( message, walkerContext.getStopCause() );

        this.walkerContext = walkerContext;
    }

    public WalkerContext getWalkerContext()
    {
        return walkerContext;
    }
}
