/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
