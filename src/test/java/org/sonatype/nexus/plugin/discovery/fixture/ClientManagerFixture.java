package org.sonatype.nexus.plugin.discovery.fixture;

import org.sonatype.nexus.plugin.discovery.NexusTestClientManager;

public final class ClientManagerFixture
    implements NexusTestClientManager
{

    public String testUrl;

    public String testUser;

    public String testPassword;

    public boolean testConnection( final String url, final String user, final String password )
    {
        return ( testUrl == null || url.equals( testUrl ) ) && ( testUser == null || user.equals( testUser ) )
            && ( testPassword == null || password.equals( testPassword ) );
    }

}
