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
package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * The event fired when a repository's proxy mode is set. This event is fired on <b>every</b> setting of ProxyMode on a
 * ProxyRepository, unlike the RepositoryEventProxyModeChanged event.
 * 
 * @author cstamas
 */
public class RepositoryEventProxyModeSet
    extends RepositoryEventProxyMode
{
    public RepositoryEventProxyModeSet( final ProxyRepository repository, final ProxyMode oldProxyMode,
                                        final ProxyMode newProxyMode, final Throwable cause )
    {
        super( repository, oldProxyMode, newProxyMode, cause );
    }
}
