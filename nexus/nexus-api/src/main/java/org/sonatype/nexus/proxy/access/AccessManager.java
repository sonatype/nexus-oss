/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
     * The implementation of this method should throw AccessDeniedException or any subclass if it denies access.
     * 
     * @param request the request
     * @param repository the repository
     * @param permission the permission
     * @throws AccessDeniedException the access denied exception
     */
    void decide( ResourceStoreRequest request, Repository repository, Action action )
        throws AccessDeniedException;

    /**
     * Authorizes an InternalAction.
     * 
     * @param request
     * @param repository
     * @param action
     * @return
     */
    boolean isAuthorizedFor( ResourceStoreRequest request, Repository repository, InternalAction action );
}
