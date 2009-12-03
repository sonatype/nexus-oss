package org.sonatype.nexus.buup;

import java.io.IOException;

import org.sonatype.nexus.buup.invoke.NexusBuupInvocationException;

public interface NexusBuupPlugin
{
    /**
     * Performs needed checks (like FS perms), and starts downloading the bundle and finally unzip it to a known place.
     */
    void initiateBundleDownload()
        throws IOException;

    /**
     * Returns true if all is set to initiate upgrade process.
     * 
     * @return
     */
    boolean isUpgradeProcessReady();

    /**
     * Initiates upgrade process. If isUpgradeReady() would return false, will do nothing, just return.
     * 
     * @return false is not all conditions met. NEVER returns true, since kills JVM
     */
    boolean initiateUpgradeProcess()
        throws NexusBuupInvocationException;
}
