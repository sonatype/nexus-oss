package org.sonatype.nexus.plugins.lvo.strategy;

import org.sonatype.nexus.SystemStatus;

public abstract class AbstractRemoteDiscoveryStrategy
    extends AbstractDiscoveryStrategy
{
    /**
     * Format's the user agent string for remote discoveries, if needed. TODO: this method is a copy+paste of the one in
     * AbstractRemoteRepositoryStorage, fix this
     */
    protected String formatUserAgent()
    {
        SystemStatus status = getNexus().getSystemStatus();

        StringBuffer userAgentPlatformInfo = new StringBuffer( "Nexus/" )
            .append( status.getVersion() ).append( " (" ).append( status.getEditionShort() ).append( "; " ).append(
                System.getProperty( "os.name" ) ).append( "; " ).append( System.getProperty( "os.version" ) ).append(
                "; " ).append( System.getProperty( "os.arch" ) ).append( "; " ).append(
                System.getProperty( "java.version" ) ).append( ") " ).append( "LVOPlugin/1.0" );

        return userAgentPlatformInfo.toString();
    }
}
