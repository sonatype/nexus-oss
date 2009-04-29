package org.sonatype.configuration;

/**
 * Generic exception thrown when there is a problem with configuration.
 * 
 * @author cstamas
 */
public class ConfigurationException
    extends Exception
{
    private static final long serialVersionUID = 8313716431404431298L;

    public ConfigurationException( String msg, Throwable t )
    {
        super( msg, t );
    }

    public ConfigurationException( String msg )
    {
        super( msg );
    }
}
