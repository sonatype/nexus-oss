package org.sonatype.nexus.plugins.lvo;

public class NoSuchKeyException
    extends Exception
{
    private static final long serialVersionUID = -3761543378260905784L;

    public NoSuchKeyException( String key )
    {
        super( "Key '" + key + "' does not exists." );
    }
}
