package org.nexus.plugins.plugin.console;

import java.util.List;

import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;

/**
 * @author juven
 */
public interface PluginConsoleManager
{
    List<PluginInfo> listPluginInfo();
}
