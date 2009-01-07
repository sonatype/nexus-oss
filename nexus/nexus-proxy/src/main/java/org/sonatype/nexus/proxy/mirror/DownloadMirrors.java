package org.sonatype.nexus.proxy.mirror;

import java.util.List;


public interface DownloadMirrors
{

    void setUrls( List<String> urls );

    /**
     * Returns list of all configured mirror urls, including urls of mirrors
     * added to the blacklist.
     */
    List<String> getUrls();

    boolean isBlacklisted( String url );

    DownloadMirrorSelector openSelector();
}
