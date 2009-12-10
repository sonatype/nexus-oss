package org.sonatype.nexus.buup;

/**
 * Thrown when an unrecoverable (from Nexus aspect) error occurs during upgrade.
 * 
 * @author cstamas
 */
public class NexusUpgradeException
    extends Exception
{
    private static final long serialVersionUID = 8424873046467883548L;

    public NexusUpgradeException( String message )
    {
        super( message );
    }

    public NexusUpgradeException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
