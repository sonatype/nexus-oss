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

/**
 * Thrown when a request is denied by Nexus for security reasons. This exception should be ALWAYS considered as
 * "authorization denied" type of stuff, since Nexus does not deal with authentication. Simply taken, this exception is
 * thrown for lack of permissions of the already authenticated subject.
 * 
 * @author cstamas
 */
public class AccessDeniedException
    extends AuthorizationException
{
    private static final long serialVersionUID = 8341250956517740603L;

    private final ResourceStoreRequest request;

    public AccessDeniedException( String msg )
    {
        super( msg );

        this.request = null;
    }

    public AccessDeniedException( ResourceStoreRequest request, String msg )
    {
        super( msg );

        this.request = request;
    }

    /**
     * The RepositoryItemUid that is forbidden to access.
     * 
     * @return
     */
    public ResourceStoreRequest getResourceStoreRequest()
    {
        return this.request;
    }
}
