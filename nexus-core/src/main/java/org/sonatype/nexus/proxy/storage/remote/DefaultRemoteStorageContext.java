/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.remote;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteHttpsProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.AbstractStorageContext;
import org.sonatype.nexus.proxy.storage.StorageContext;

/**
 * The default remote storage context.
 * 
 * @author cstamas
 */
public class DefaultRemoteStorageContext
    extends AbstractStorageContext
    implements RemoteStorageContext
{
    public DefaultRemoteStorageContext( final StorageContext parent )
    {
        super( parent );
    }

    @Override
    public boolean hasRemoteAuthenticationSettings()
    {
        return hasContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    @Override
    public RemoteAuthenticationSettings getRemoteAuthenticationSettings()
    {
        return (RemoteAuthenticationSettings) getContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    @Override
    public void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings )
    {
        putContextObject( RemoteAuthenticationSettings.class.getName(), settings );
    }

    @Override
    public void removeRemoteAuthenticationSettings()
    {
        removeContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    @Override
    public boolean hasRemoteConnectionSettings()
    {
        return hasContextObject( RemoteConnectionSettings.class.getName() );
    }

    @Override
    public RemoteConnectionSettings getRemoteConnectionSettings()
    {
        return (RemoteConnectionSettings) getContextObject( RemoteConnectionSettings.class.getName() );
    }

    @Override
    public void setRemoteConnectionSettings( RemoteConnectionSettings settings )
    {
        putContextObject( RemoteConnectionSettings.class.getName(), settings );
    }

    @Override
    public void removeRemoteConnectionSettings()
    {
        removeContextObject( RemoteConnectionSettings.class.getName() );
    }

    @Override
    public boolean hasRemoteProxySettings()
    {
        return hasContextObject( RemoteProxySettings.class.getName() );
    }

    @Override
    public RemoteProxySettings getRemoteProxySettings()
    {
        // we have a special case here, need to track blockInheritance flag
        // so, a little code duplication happens
        // three cases:
        // 1. we have _no_ proxy settings in this context, fallback to original code
        // 2. we have proxy settings with no proxyHost set, then obey the blockInheritance
        // 3. we have proxy settings with set proxyHost, then return it

        final String key = RemoteProxySettings.class.getName();

        if ( !hasContextObject( key ) )
        {
            // case 1
            return (RemoteProxySettings) getContextObject( key );
        }
        else
        {
            RemoteProxySettings remoteProxySettings = (RemoteProxySettings) getContextObject( key, false );

            if ( StringUtils.isBlank( remoteProxySettings.getHostname() ) )
            {
                // case 2
                if ( !remoteProxySettings.isBlockInheritance() )
                {
                    return (RemoteProxySettings) getContextObject( key );
                }
                else
                {
                    // no proxy on this level, and do _not_ inherit
                    return null;
                }
            }
            else
            {
                // case 3
                return remoteProxySettings;
            }
        }
    }

    @Override
    public void setRemoteProxySettings( RemoteProxySettings settings )
    {
        putContextObject( RemoteProxySettings.class.getName(), settings );
    }

    @Override
    public void removeRemoteProxySettings()
    {
        removeContextObject( RemoteProxySettings.class.getName() );
    }

    /**
     * @since 2.5
     */
    @Override
    public boolean hasRemoteHttpsProxySettings()
    {
        return hasContextObject( RemoteHttpsProxySettings.class.getName() );
    }

    /**
     * @since 2.5
     */
    @Override
    public RemoteProxySettings getRemoteHttpsProxySettings()
    {
        // we have a special case here, need to track blockInheritance flag
        // so, a little code duplication happens
        // four cases:
        // 1. we have NO HTTPS proxy settings in this context but we have HTTP proxy settings, use HTTP proxy settings
        // 2. we have NO HTTPS proxy settings in this context, fallback to original code
        // 3. we have HTTPS proxy settings with no proxyHost set, then obey the blockInheritance
        // 4. we have HTTPS proxy settings with set proxyHost, then return it

        final String key = RemoteHttpsProxySettings.class.getName();

        if ( !hasContextObject( key ) )
        {
            final String keyHttp = RemoteProxySettings.class.getName();
            if ( hasContextObject( keyHttp ) )
            {
                // case 1
                return (RemoteProxySettings) getContextObject( keyHttp );
            }
            else
            {
                // case 2
                return (RemoteProxySettings) getContextObject( key );
            }
        }
        else
        {
            RemoteProxySettings remoteProxySettings = (RemoteProxySettings) getContextObject( key, false );

            if ( StringUtils.isBlank( remoteProxySettings.getHostname() ) )
            {
                // case 3
                if ( !remoteProxySettings.isBlockInheritance() )
                {
                    return (RemoteProxySettings) getContextObject( key );
                }
                else
                {
                    // no proxy on this level, and do _not_ inherit
                    return null;
                }
            }
            else
            {
                // case 4
                return remoteProxySettings;
            }
        }
    }

    /**
     * @since 2.5
     */
    @Override
    public void setRemoteHttpsProxySettings( RemoteProxySettings settings )
    {
        putContextObject( RemoteHttpsProxySettings.class.getName(), settings );
    }

    /**
     * @since 2.5
     */
    @Override
    public void removeRemoteHttpsProxySettings()
    {
        removeContextObject( RemoteHttpsProxySettings.class.getName() );
    }

    // ==

    /**
     * Simple helper class to have boolean stored in context and not disturbing the update of it.
     */
    public static class BooleanFlagHolder
    {
        private Boolean flag = null;

        /**
         * Returns true only and if only flag is not null and has value Boolean.TRUE.
         * 
         * @return
         */
        public boolean isFlag()
        {
            if ( flag != null )
            {
                return flag;
            }
            else
            {
                return false;
            }
        }

        public boolean isNull()
        {
            return flag == null;
        }

        public void setFlag( Boolean flag )
        {
            this.flag = flag;
        }
    }
}
