package org.sonatype.nexus.plugins;

import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;

/**
 * The plugin repository context. UNDER HEAVY CONSTRUCTION, this is in flux! Please consider major changes on this
 * inferface!
 * 
 * @author cstamas
 */
public interface PluginContext
{
    ApplicationEventMulticaster getApplicationEventMulticaster();

    RepositoryTypeRegistry getRepositoryTypeRegistry();

    RepositoryRegistry getRepositoryRegistry();
}
