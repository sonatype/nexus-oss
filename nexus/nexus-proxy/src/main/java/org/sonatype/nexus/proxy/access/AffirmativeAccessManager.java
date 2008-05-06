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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Simple implementation of AccessManager that passes only if all voter votes ACCESS_APPROVED.
 * 
 * @author t.cservenak
 * @plexus.component instantiation-strategy="per-lookup" role-hint="affirmative"
 */
public class AffirmativeAccessManager
    implements AccessManager
{

    /** The voters. */
    private List<AccessDecisionVoter> voters = new ArrayList<AccessDecisionVoter>();

    /**
     * Gets the voters.
     * 
     * @return the voters
     */
    public List<AccessDecisionVoter> getVoters()
    {
        return voters;
    }

    /**
     * Sets the voters.
     * 
     * @param voters the new voters
     */
    public void setVoters( List<AccessDecisionVoter> voters )
    {
        this.voters = voters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.access.AccessManager#decide(org.sonatype.nexus.ProximityRequest,
     *      org.sonatype.nexus.Repository, org.sonatype.nexus.access.RepositoryPermission)
     */
    public void decide( ResourceStoreRequest request, Repository repository, RepositoryPermission permission )
        throws AccessDeniedException
    {
        for ( AccessDecisionVoter voter : voters )
        {
            if ( voter.vote( request, repository, permission ) != AccessDecisionVoter.ACCESS_APPROVED )
            {
                throw new AccessDeniedException(
                    new RepositoryItemUid( repository, request.getRequestPath() ),
                    "Voter " + voter.getClass().getName() + " has voted against access." );
            }
        }

    }

}
