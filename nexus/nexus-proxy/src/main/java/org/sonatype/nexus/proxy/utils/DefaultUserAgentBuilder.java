package org.sonatype.nexus.proxy.utils;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

@Component( role = UserAgentBuilder.class )
public class DefaultUserAgentBuilder
    implements UserAgentBuilder
{
    @Requirement
    private ApplicationStatusSource applicationStatusSource;

    /**
     * The edition, that will tell us is there some change happened with installation.
     */
    private String platformEditionShort;

    /**
     * The lazily calculated invariant part of the UserAgentString.
     */
    private String userAgentPlatformInfo;

    @Override
    public String formatGenericUserAgentString()
    {
        return getUserAgentPlatformInfo();
    }

    @Override
    public String formatRemoteRepositoryStorageUserAgentString( final ProxyRepository repository,
                                                                final RemoteStorageContext ctx )
    {
        final StringBuffer buf = new StringBuffer( getUserAgentPlatformInfo() );

        final RemoteRepositoryStorage rrs = repository.getRemoteStorage();

        buf.append( " " ).append( rrs.getProviderId() ).append( "/" ).append( rrs.getVersion() );

        // user customization
        RemoteConnectionSettings remoteConnectionSettings = ctx.getRemoteConnectionSettings();

        if ( !StringUtils.isEmpty( remoteConnectionSettings.getUserAgentCustomizationString() ) )
        {
            buf.append( " " ).append( remoteConnectionSettings.getUserAgentCustomizationString() );
        }

        return buf.toString();
    }

    // ==

    protected synchronized String getUserAgentPlatformInfo()
    {
        // TODO: this is a workaround, see NXCM-363
        SystemStatus status = applicationStatusSource.getSystemStatus();

        if ( platformEditionShort == null || !platformEditionShort.equals( status.getEditionShort() )
            || userAgentPlatformInfo == null )
        {
            platformEditionShort = status.getEditionShort();

            userAgentPlatformInfo =
                new StringBuffer( "Nexus/" ).append( status.getVersion() ).append( " (" ).append(
                    status.getEditionShort() ).append( "; " ).append( System.getProperty( "os.name" ) ).append( "; " ).append(
                    System.getProperty( "os.version" ) ).append( "; " ).append( System.getProperty( "os.arch" ) ).append(
                    "; " ).append( System.getProperty( "java.version" ) ).append( ")" ).toString();
        }

        return userAgentPlatformInfo;
    }

}
