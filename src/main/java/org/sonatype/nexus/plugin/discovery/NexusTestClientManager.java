package org.sonatype.nexus.plugin.discovery;

public interface NexusTestClientManager
{

    boolean testConnection( String url, String user, String password );

}
