/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sonatype.nexus.proxy.repository.GroupRepository;

/**
 * Fired when a group repository members changed and is applied (not rollbacked).
 * 
 * @author cstamas
 */
public class RepositoryGroupMembersChangedEvent
    extends RepositoryEvent
{
    public enum MemberChange
    {
        MEMBER_ADDED, MEMBER_REMOVED, MEMBER_REORDERED;
    }

    private final List<String> oldRepositoryMemberIds;

    private final List<String> newRepositoryMemberIds;

    private final List<String> removedRepositoryIds;

    private final List<String> addedRepositoryIds;

    private final List<String> reorderedRepositoryIds;

    private final Set<MemberChange> memberChangeSet;

    public RepositoryGroupMembersChangedEvent( final GroupRepository repository, final List<String> currentMemberIds,
                                               final List<String> newMemberIds )
    {
        super( repository );
        // we need to copy these to "detach" them for sure from config
        this.oldRepositoryMemberIds = new ArrayList<String>( currentMemberIds );
        this.newRepositoryMemberIds = new ArrayList<String>( newMemberIds );

        // simple calculations
        this.removedRepositoryIds = new ArrayList<String>( oldRepositoryMemberIds );
        removedRepositoryIds.removeAll( newRepositoryMemberIds );
        this.addedRepositoryIds = new ArrayList<String>( newRepositoryMemberIds );
        addedRepositoryIds.removeAll( oldRepositoryMemberIds );
        this.reorderedRepositoryIds = new ArrayList<String>();

        // ordering detection
        final List<String> currentTrimmed = new ArrayList<String>( oldRepositoryMemberIds );
        currentTrimmed.removeAll( removedRepositoryIds );
        currentTrimmed.removeAll( addedRepositoryIds );
        final List<String> newTrimmed = new ArrayList<String>( newRepositoryMemberIds );
        newTrimmed.removeAll( removedRepositoryIds );
        newTrimmed.removeAll( addedRepositoryIds );

        if ( !currentTrimmed.equals( newTrimmed ) && currentTrimmed.size() > 0 )
        {
            Iterator<String> i1 = currentTrimmed.iterator();
            Iterator<String> i2 = newTrimmed.iterator();

            while ( i1.hasNext() )
            {
                final String oldEl = i1.next();
                final String newEl = i2.next();
                if ( !oldEl.equals( newEl ) )
                {
                    reorderedRepositoryIds.add( newEl );
                }
            }
        }

        this.memberChangeSet = EnumSet.noneOf( MemberChange.class );
        if ( !reorderedRepositoryIds.isEmpty() )
        {
            memberChangeSet.add( MemberChange.MEMBER_REORDERED );
        }
        if ( !removedRepositoryIds.isEmpty() )
        {
            memberChangeSet.add( MemberChange.MEMBER_REMOVED );
        }
        if ( !addedRepositoryIds.isEmpty() )
        {
            memberChangeSet.add( MemberChange.MEMBER_ADDED );
        }
    }

    public GroupRepository getGroupRepository()
    {
        return (GroupRepository) getEventSender();
    }

    public Set<MemberChange> getMemberChangeSet()
    {
        return memberChangeSet;
    }

    public List<String> getOldRepositoryMemberIds()
    {
        return Collections.unmodifiableList( oldRepositoryMemberIds );
    }

    public List<String> getNewRepositoryMemberIds()
    {
        return Collections.unmodifiableList( newRepositoryMemberIds );
    }

    public List<String> getRemovedRepositoryIds()
    {
        return Collections.unmodifiableList( removedRepositoryIds );
    }

    public List<String> getAddedRepositoryIds()
    {
        return Collections.unmodifiableList( addedRepositoryIds );
    }

    public List<String> getReorderedRepositoryIds()
    {
        return Collections.unmodifiableList( reorderedRepositoryIds );
    }
}
