/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.storage.remote;

import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;

/**
 * The remote storage settings and context.
 * 
 * @author cstamas
 */
public interface RemoteStorageContext
{
    // change detection

    /**
     * Returns the timestamp of latest change. Will propagate to parent if it is more recently changed as this (and
     * parent is set).
     */
    long getLastChanged();

    // parent

    /**
     * Returns the parent context, or null if not set.
     * 
     * @return
     */
    RemoteStorageContext getParentRemoteStorageContext();

    // modification

    /**
     * Gets an object from context. Will propagate to parent if not found in this context (and parent is set).
     * 
     * @param key
     * @return
     */
    Object getRemoteConnectionContextObject( String key );

    /**
     * Puts an object into this context.
     * 
     * @param key
     * @param value
     */
    void putRemoteConnectionContextObject( String key, Object value );

    /**
     * Removed an object from this context. Parent is unchanged.
     * 
     * @param key
     */
    void removeRemoteConnectionContextObject( String key );

    /**
     * Returns true if this context has an object under the given key.
     * 
     * @param key
     * @return
     */
    boolean hasRemoteConnectionContextObject( String key );

    // --

    boolean hasRemoteConnectionSettings();

    RemoteConnectionSettings getRemoteConnectionSettings();

    void setRemoteConnectionSettings( RemoteConnectionSettings settings );

    void removeRemoteConnectionSettings();

    boolean hasRemoteAuthenticationSettings();

    RemoteAuthenticationSettings getRemoteAuthenticationSettings();

    void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings );

    void removeRemoteAuthenticationSettings();

    boolean hasRemoteProxySettings();

    RemoteProxySettings getRemoteProxySettings();

    void setRemoteProxySettings( RemoteProxySettings settings );

    void removeRemoteProxySettings();
}
