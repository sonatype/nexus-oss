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
package org.sonatype.nexus.proxy;

import java.net.URL;

import org.sonatype.nexus.proxy.repository.ProxyRepository;

/**
 * Thrown when a request is denied by remote peer for security reasons (ie. HTTP RemoteRepositoryStorage gets 403
 * response code).
 * 
 * @author cstamas
 */
public class RemoteAccessDeniedException
    extends RemoteAccessException
{
    private static final long serialVersionUID = -4719375204384900503L;

    private final URL url;

    public RemoteAccessDeniedException( ProxyRepository repository, URL url, String message )
    {
        this( repository, url, message, null );
    }

    public RemoteAccessDeniedException( ProxyRepository repository, URL url, String message, Throwable cause )
    {
        super( repository, message, cause );

        this.url = url;
    }

    public URL getUrl()
    {
        return url;
    }

}
