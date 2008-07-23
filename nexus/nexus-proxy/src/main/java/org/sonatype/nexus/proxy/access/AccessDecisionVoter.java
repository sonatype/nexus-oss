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

import java.util.Map;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Interface AccessDecisionVoter.
 */
public interface AccessDecisionVoter
{
    String ROLE = AccessDecisionVoter.class.getName();

    /** Vote for approval. */
    static final int ACCESS_APPROVED = 1;

    /** Vote for neutral status. */
    static final int ACCESS_NEUTRAL = 0;

    /** Vote for denial. */
    static final int ACCESS_DENIED = -1;

    /** The Constant REQUEST_USERNAME key. */
    public static final String REQUEST_USER = "request.user";

    /** Passes the configuration to voter */
    void setConfiguration( Map<String, String> config )
        throws InterpolationException;

    /**
     * The implementation of this method should return one of the ACCESS_APPROVED, ACCESS_NEUTRAL or ACCESS_DENIED
     * constants.
     * 
     * @param request the request
     * @param repository the repository
     * @param permission the permission
     * @return The vote for this access.
     */
    int vote( ResourceStoreRequest request, Repository repository, RepositoryPermission permission );

}
