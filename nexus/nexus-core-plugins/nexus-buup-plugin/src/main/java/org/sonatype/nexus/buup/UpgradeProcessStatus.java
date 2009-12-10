package org.sonatype.nexus.buup;

public enum UpgradeProcessStatus
{
    /**
     * Not invoked at all.
     */
    UNUSED,

    /**
     * Download is in progress.
     */
    DOWNLOADING,

    /**
     * Downloaded successfully, all conditions are met to invoke BUUP, ready to run.
     */
    READY_TO_RUN,

    /**
     * Something failed, that is not recoverable by Nexus (for example FS perms, bundle not found, transport error).
     * Check logs for details. User may "interact" (ie. fix FS perms, or plug the network cable in) and retry the
     * operation.
     */
    FAILED;

    /**
     * Returns true if the process may be started from "beginning".
     * 
     * @return
     */
    public boolean isStartingState()
    {
        return UNUSED.equals( this ) || FAILED.equals( this );
    }
}
