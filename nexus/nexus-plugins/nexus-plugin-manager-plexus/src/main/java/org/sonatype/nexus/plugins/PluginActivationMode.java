package org.sonatype.nexus.plugins;

public enum PluginActivationMode
{
    /** Let Nexus decide what to do */
    AUTO,
    
    /** User enabled it manually (ie. because AUTO would reject it) */
    MANUAL_ENABLED,

    /** User disabled it manually */
    MANUAL_DISABLED;
}
