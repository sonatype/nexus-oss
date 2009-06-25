package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.plugin.ExtensionPoint;

/**
 * A hosted repository that serves up "web" content (static HTML files). Default behaviour: If a request results in
 * collection, it will look in that collection for any existing welcome file and serve that up instead of collection. If
 * no welcome file found, it falls back to collection/index view.
 * 
 * @author cstamas
 */
@ExtensionPoint
public interface WebSiteRepository
    extends HostedRepository
{
    /**
     * Gets the list of unmodifiable "welcome" file names. Example: "index.html", "index.htm".
     * 
     * @return
     */
    List<String> getWelcomeFiles();

    /**
     * Sets the list of welcome files.
     * 
     * @param files
     */
    void setWelcomeFiles( List<String> files );
}
