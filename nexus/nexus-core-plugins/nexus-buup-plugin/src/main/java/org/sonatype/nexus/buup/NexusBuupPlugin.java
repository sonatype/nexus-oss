package org.sonatype.nexus.buup;

import java.io.IOException;
import java.util.Collection;

import org.sonatype.nexus.buup.invoke.NexusBuupInvocationException;

public interface NexusBuupPlugin
{
    /**
     * Performs needed checks (like FS perms), and starts downloading the bundle and finally unzip it to a known place.
     */
    void initiateBundleDownload()
        throws NexusUpgradeException;

    /**
     * Returns the status of process.
     * 
     * @return
     */
    UpgradeProcessStatus getUpgradeProcessStatus();

    /**
     * Returns the list of failure reasons. This list is only populated if the getUpgradeProcessStatus() method returns
     * FAIL.
     * 
     * @return
     */
    Collection<IOException> getFailureReasons();

    /**
     * Initiates upgrade process. If isUpgradeReady() would return false, will do nothing, just return.
     * 
     * @return false is not all conditions met. NEVER returns true, since kills JVM
     */
    void initiateUpgradeProcess()
        throws NexusUpgradeException, NexusBuupInvocationException;
}
