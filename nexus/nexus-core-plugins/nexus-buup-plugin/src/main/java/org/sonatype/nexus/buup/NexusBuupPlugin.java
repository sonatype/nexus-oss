package org.sonatype.nexus.buup;

public interface NexusBuupPlugin
{
    void initiateBundleDownload();

    void initiateUpgradeProcess();
}
