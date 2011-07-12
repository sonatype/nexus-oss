package org.sonatype.nexus.plugin.discovery.fixture;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.plugin.ExpectPrompter;
import org.sonatype.nexus.plugin.discovery.DefaultNexusDiscovery;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

public class DefaultDiscoveryFixture
    extends DefaultNexusDiscovery
{

    public DefaultDiscoveryFixture( final SecDispatcher secDispatcher, final ExpectPrompter prompter,
                                    final Logger logger )
    {
        super( new ClientManagerFixture(), secDispatcher, prompter, logger );
    }

}
