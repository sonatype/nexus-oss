package org.sonatype.nexus.plugins.lvo.config;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.lvo.NoSuchKeyException;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;

public interface LvoPluginConfiguration
{
    CLvoKey getLvoKey( String key )
        throws NoSuchKeyException;

    boolean isEnabled();

    /**
     * Disable the plugin from going remote and checking for new version
     */
    void disable()
        throws ConfigurationException,
            IOException;

    /**
     * Enable the remote check for new versions
     */
    void enable()
        throws ConfigurationException,
            IOException;
}
