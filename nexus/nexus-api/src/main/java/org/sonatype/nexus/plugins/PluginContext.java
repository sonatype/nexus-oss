package org.sonatype.nexus.plugins;

import java.io.File;

/**
 * The plugin repository context. UNDER HEAVY CONSTRUCTION, this is in flux! Please consider major changes on this
 * inferface!
 * 
 * @author cstamas
 */
public interface PluginContext
{
    /**
     * The directory on disk provided for the plugin to use for persistencing extra stuff if needed.
     * 
     * @return
     */
    File getBasedir();
}
