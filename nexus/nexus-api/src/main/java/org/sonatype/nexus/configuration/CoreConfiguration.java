package org.sonatype.nexus.configuration;

/**
 * Top level interface for wrapping up "core configurations", those used by core components.
 * 
 * @author cstamas
 */
public interface CoreConfiguration
    extends RevertableConfiguration
{
    /**
     * Returns the external configuration, if any. Null otherwise.
     * 
     * @return
     */
    ExternalConfiguration getExternalConfiguration();
}
