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
package org.sonatype.nexus.proxy.access;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Interface for access manager.
 * 
 * @author t.cservenak
 */
public interface AccessManager
{
    /**
     * Key used for authenticated username in request.
     */
    String REQUEST_USER = "request.user";

    /**
     * Key used for request source address.
     */
    String REQUEST_REMOTE_ADDRESS = "request.address";

    /**
     * Key used to mark is the request coming over confidential channel (https).
     */
    String REQUEST_CONFIDENTIAL = "request.isConfidential";

    /**
     * Key used to mark the request certificates of confidential channel (https).
     */
    String REQUEST_CERTIFICATES = "request.certificates";
    
    /**
     * Key used to mark a request as already authorized, if set, no authorization will be performed
     */
    String REQUEST_AUTHORIZED = "request.authorized";

    /**
     * Key used for authenticated user agent in request.
     */
    String REQUEST_AGENT = "request.agent";

    /**
     * The implementation of this method should throw AccessDeniedException or any subclass if it denies access.
     * 
     * @throws AccessDeniedException the access denied exception
     */
    void decide( Repository repository, ResourceStoreRequest request, Action action )
        throws AccessDeniedException;
}
