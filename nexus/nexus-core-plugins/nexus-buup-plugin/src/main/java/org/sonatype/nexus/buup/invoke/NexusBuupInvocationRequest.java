package org.sonatype.nexus.buup.invoke;

import java.io.File;

/**
 * A request object that contains the BUUP invocation details.
 * 
 * @author cstamas
 */
public class NexusBuupInvocationRequest
{
    /**
     * Value to be interpreted as "unchanged", so user did not set any specific value. BUUP should leave the memory
     * option unchanged.
     */
    public static final int XM_UNCHANGED = -1;

    /**
     * The directory where the downloaded upgrade bundle is unzipped.
     */
    private final File explodedUpgradeBundleDirectory;

    /**
     * The amount of memory to pass in as -Xmx parameter. Should be as JVM CLI specifies, but without the "-Xmx" prefix!
     * The number should be interpreted as MB. Example: 512 means 512MB
     */
    private int nexusBundleXmx = XM_UNCHANGED;

    /**
     * The amount of memory to pass in as -Xms parameter. Should be as JVM CLI specifies, but without the "-Xms" prefix!
     * The number should be interpreted as MB. Example: 128 means 128MB
     */
    private int nexusBundleXms = XM_UNCHANGED;

    public NexusBuupInvocationRequest( File explodedUpgradeBundleDirectory )
    {
        this.explodedUpgradeBundleDirectory = explodedUpgradeBundleDirectory;
    }

    public File getExplodedUpgradeBundleDirectory()
    {
        return explodedUpgradeBundleDirectory;
    }

    public int getNexusBundleXmx()
    {
        return nexusBundleXmx;
    }

    public void setNexusBundleXmx( int nexusBundleXmx )
    {
        this.nexusBundleXmx = nexusBundleXmx;
    }

    public int getNexusBundleXms()
    {
        return nexusBundleXms;
    }

    public void setNexusBundleXms( int nexusBundleXms )
    {
        this.nexusBundleXms = nexusBundleXms;
    }
}
