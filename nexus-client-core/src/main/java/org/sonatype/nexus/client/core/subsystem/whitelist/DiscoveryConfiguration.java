package org.sonatype.nexus.client.core.subsystem.whitelist;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The whitelist discovery configuration for a proxy repository.
 * 
 * @author cstamas
 * @since 2.4
 */
public class DiscoveryConfiguration
{
    private boolean enabled;

    private int intervalHours;

    /**
     * Constructor.
     * 
     * @param enabled
     * @param intervalHours
     */
    public DiscoveryConfiguration( final boolean enabled, final int intervalHours )
    {
        setEnabled( enabled );
        setIntervalHours( intervalHours );
    }

    /**
     * Returns {@code true} if discovery is enabled.
     * 
     * @return {@code true} if enabled.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Toggles the enabled state of discovery.
     * 
     * @param enabled
     */
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }

    /**
     * Returns the amount of hours that makes the interval of discovery updates.
     * 
     * @return hours of the interval.
     */
    public int getIntervalHours()
    {
        return intervalHours;
    }

    /**
     * Sets the interval of discovery updates, in hours.
     * 
     * @param intervalHours
     */
    public void setIntervalHours( int intervalHours )
    {
        checkArgument( intervalHours >= 1 );
        this.intervalHours = intervalHours;
    }
}
