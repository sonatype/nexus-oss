/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration;

import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;

/**
 * A component to convert various repository status enumerations to configuration values.
 * 
 * @author cstamas
 */
public interface RepositoryStatusConverter
{
    LocalStatus localStatusFromModel( String string );

    String localStatusToModel( LocalStatus localStatus );

    ProxyMode proxyModeFromModel( String string );

    String proxyModeToModel( ProxyMode proxyMode );

    String remoteStatusToModel( RemoteStatus remoteStatus );
}
