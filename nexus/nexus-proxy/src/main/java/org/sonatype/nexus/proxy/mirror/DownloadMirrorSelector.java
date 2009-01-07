package org.sonatype.nexus.proxy.mirror;

import java.util.List;

public interface DownloadMirrorSelector
{

    /**
     * Returns possibly empty list of available urls.
     */
    List<String> getUrls();

    /**
     * Requested item was successfully downloaded from specified mirror url.
     */
    void feedbackSuccess( String url );

    /**
     * There was a problem (like IOException or ItemNotFound) retrieving requested item from specified mirror url.
     * 
     * @throws IllegalStateException if there is no selected mirror.
     */
    void feedbackFailure( String url );

    /**
     * Updates mirror statistics and closes this mirror selector.
     */
    void close();

}
