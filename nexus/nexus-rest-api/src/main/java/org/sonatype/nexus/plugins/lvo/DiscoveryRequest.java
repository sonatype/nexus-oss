package org.sonatype.nexus.plugins.lvo;

import org.sonatype.nexus.plugins.lvo.config.model.CLvoKey;

public class DiscoveryRequest
{
    private final String key;

    private final CLvoKey lvoKey;

    public DiscoveryRequest( String key, CLvoKey lvoKey )
    {
        this.key = key;

        this.lvoKey = lvoKey;
    }

    public String getKey()
    {
        return key;
    }

    public CLvoKey getLvoKey()
    {
        return lvoKey;
    }
}
