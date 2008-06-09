package org.sonatype.nexus;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

/**
 * Various services controlled by Nexus.
 * 
 * @author cstamas
 */
public interface NexusService
{
    void startService()
        throws StartingException;

    void stopService()
        throws StoppingException;
}
