package org.sonatype.nexus.proxy.repository;

import java.util.List;

/**
 * A hosted repository that serves up "web" content (static HTML files).
 * 
 * @author cstamas
 */
public interface WebSiteRepository
    extends HostedRepository
{
    List<String> getWelcomeFiles();
}
