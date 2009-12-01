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
     * The directory where the downloaded upgrade bundle is unzipped.
     */
    private final File explodedUpgradeBundleDirectory;

    /**
     * The amount of memory to pass in as -Xmx parameter. Should be as JVM CLI specifies, but without the "-Xmx" prefix!
     * Example: 512m means 512MB
     */
    private String nexusBundleXmx;

    /**
     * The amount of memory to pass in as -Xms parameter. Should be as JVM CLI specifies, but without the "-Xms" prefix!
     * Example: 128m means 128MB
     */
    private String nexusBundleXms;

    public NexusBuupInvocationRequest( File explodedUpgradeBundleDirectory )
    {
        this.explodedUpgradeBundleDirectory = explodedUpgradeBundleDirectory;
    }

    public File getExplodedUpgradeBundleDirectory()
    {
        return explodedUpgradeBundleDirectory;
    }

    public String getNexusBundleXmx()
    {
        return nexusBundleXmx;
    }

    public void setNexusBundleXmx( String nexusBundleXmx )
    {
        this.nexusBundleXmx = nexusBundleXmx;
    }

    public String getNexusBundleXms()
    {
        return nexusBundleXms;
    }

    public void setNexusBundleXms( String nexusBundleXms )
    {
        this.nexusBundleXms = nexusBundleXms;
    }
}
