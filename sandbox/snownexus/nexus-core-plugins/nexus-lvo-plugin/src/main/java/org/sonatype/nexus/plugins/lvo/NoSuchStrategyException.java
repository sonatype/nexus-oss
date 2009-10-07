package org.sonatype.nexus.plugins.lvo;

public class NoSuchStrategyException
    extends Exception
{
    private static final long serialVersionUID = -3761543378260905784L;

    public NoSuchStrategyException( String key )
    {
        super( "Strategy '" + key + "' does not exists." );
    }
}
