package org.sonatype.nexus.buup.invoke;

import java.io.File;

/**
 * A request object that contains the BUUP invocation details.
 * 
 * @author cstamas
 */
public class NexusBuupInvocationRequest
{
    private final File explodedUpgradeBundleDirectory;

    private String nexusBundleXmx;

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
