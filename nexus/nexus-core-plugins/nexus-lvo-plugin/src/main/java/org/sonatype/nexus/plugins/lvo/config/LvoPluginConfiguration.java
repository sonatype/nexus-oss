package org.sonatype.nexus.plugins.lvo.config;

import org.sonatype.nexus.plugins.lvo.NoSuchKeyException;
import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;

public interface LvoPluginConfiguration
{
    CLvoKey getLvoKey( String key )
        throws NoSuchKeyException;
    
    boolean isEnabled();
}
