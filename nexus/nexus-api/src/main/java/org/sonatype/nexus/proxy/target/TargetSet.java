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
package org.sonatype.nexus.proxy.target;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A simple helper Set implementation.
 * 
 * @author cstamas
 */
public class TargetSet
{
    private final Set<TargetMatch> matches = new HashSet<TargetMatch>();

    private final Set<String> matchedRepositoryIds = new HashSet<String>();

    public Set<TargetMatch> getMatches()
    {
        return Collections.unmodifiableSet( matches );
    }

    public Set<String> getMatchedRepositoryIds()
    {
        return Collections.unmodifiableSet( matchedRepositoryIds );
    }

    public void addTargetMatch( TargetMatch tm )
    {
        // TODO: a very crude solution!
        for ( TargetMatch t : matches )
        {
            if ( t.getTarget().equals( tm.getTarget() )
                && t.getRepository().getId().equals( tm.getRepository().getId() ) )
            {
                return;
            }

        }

        matches.add( tm );

        matchedRepositoryIds.add( tm.getRepository().getId() );
    }

    public void addTargetSet( TargetSet ts )
    {
        if ( ts == null )
        {
            return;
        }

        for ( TargetMatch tm : ts.getMatches() )
        {
            addTargetMatch( tm );
        }
    }

    boolean isPathContained( Repository repository, String path )
    {
        for ( TargetMatch targetMatch : matches )
        {
            if ( targetMatch.getRepository().getId().equals( repository.getId() )
                && targetMatch.getTarget().isPathContained( repository.getRepositoryContentClass(), path ) )
            {
                return true;
            }
        }

        return false;
    }
}
