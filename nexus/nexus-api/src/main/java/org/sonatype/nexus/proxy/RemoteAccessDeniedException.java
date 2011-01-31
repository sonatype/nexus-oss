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

    private final String url;

    public RemoteAccessDeniedException( ProxyRepository repository, URL url, String message )
    {
        this( repository, url.toString(), message );
    }

    public RemoteAccessDeniedException( ProxyRepository repository, URL url, String message, Throwable cause )
    {
        this( repository, url.toString(), message, cause );
    }

    public RemoteAccessDeniedException( ProxyRepository repository, String url, String message )
    {
        this( repository, url, message, null );
    }

    public RemoteAccessDeniedException( ProxyRepository repository, String url, String message, Throwable cause )
    {
        super( repository, message, cause );

        this.url = url;
    }

    public String getRemoteUrl()
    {
        return url;
    }

}
