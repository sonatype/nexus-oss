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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent.MemberChange;
import org.sonatype.nexus.proxy.repository.GroupRepository;

public class RepositoryGroupMembersChangedEventTest
{
    protected RepositoryGroupMembersChangedEvent createEvent( final List<String> currentMemberIds,
                                                              final List<String> newMemberIds )
    {
        final RepositoryGroupMembersChangedEvent evt =
            new RepositoryGroupMembersChangedEvent( Mockito.mock( GroupRepository.class ), currentMemberIds,
                newMemberIds );

        assertThat( evt.getOldRepositoryMemberIds(), contains( currentMemberIds.toArray() ) );
        assertThat( evt.getNewRepositoryMemberIds(), contains( newMemberIds.toArray() ) );

        return evt;
    }

    @Test
    public void testNoChange()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "a", "b", "c" );
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 0 ) );
        assertThat( evt.getAddedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getRemovedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getReorderedRepositoryIds().size(), equalTo( 0 ) );
    }

    @Test
    public void testSimpleAddition()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "a", "b", "c", "d" );
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 1 ) );
        assertThat( evt.getMemberChangeSet(), contains( MemberChange.MEMBER_ADDED ) );
        assertThat( evt.getAddedRepositoryIds(), contains( "d" ) );
        assertThat( evt.getRemovedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getReorderedRepositoryIds().size(), equalTo( 0 ) );
    }

    @Test
    public void testSimpleAdditionMore()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "a", "b", "c", "d", "e" );
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 1 ) );
        assertThat( evt.getMemberChangeSet(), contains( MemberChange.MEMBER_ADDED ) );
        assertThat( evt.getAddedRepositoryIds(), contains( "d", "e" ) );
        assertThat( evt.getRemovedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getReorderedRepositoryIds().size(), equalTo( 0 ) );
    }

    @Test
    public void testSimpleRemoval()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "a", "b" );
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 1 ) );
        assertThat( evt.getMemberChangeSet(), contains( MemberChange.MEMBER_REMOVED ) );
        assertThat( evt.getAddedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getRemovedRepositoryIds(), contains( "c" ) );
        assertThat( evt.getReorderedRepositoryIds().size(), equalTo( 0 ) );
    }

    @Test
    public void testSimpleRemovalMore()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "a" );
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 1 ) );
        assertThat( evt.getMemberChangeSet(), contains( MemberChange.MEMBER_REMOVED ) );
        assertThat( evt.getAddedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getRemovedRepositoryIds(), contains( "b", "c" ) );
        assertThat( evt.getReorderedRepositoryIds().size(), equalTo( 0 ) );
    }

    @Test
    public void testSimpleOrderChange()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "a", "c", "b" );
        // we swapped places of "b" and "c"
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 1 ) );
        assertThat( evt.getMemberChangeSet(), contains( MemberChange.MEMBER_REORDERED ) );
        assertThat( evt.getAddedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getRemovedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getReorderedRepositoryIds(), contains( "c", "b" ) );
    }

    @Test
    public void testSimpleOrderChangeMore()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "c", "a", "b" );
        // we swapped places of all members
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 1 ) );
        assertThat( evt.getMemberChangeSet(), contains( MemberChange.MEMBER_REORDERED ) );
        assertThat( evt.getAddedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getRemovedRepositoryIds().size(), equalTo( 0 ) );
        assertThat( evt.getReorderedRepositoryIds(), contains( "c", "a", "b" ) );
    }

    @Test
    public void testMultiChange1()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c" );
        final List<String> newMembers = Arrays.asList( "d", "e", "a" );
        // we removed "b", "c", added "d", "e", and ordering of "a" did not change (it is only one remnant from old
        // list)
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 2 ) );
        assertThat( evt.getMemberChangeSet(),
            containsInAnyOrder( MemberChange.MEMBER_ADDED, MemberChange.MEMBER_REMOVED ) );
        assertThat( evt.getAddedRepositoryIds(), contains( "d", "e" ) );
        assertThat( evt.getRemovedRepositoryIds(), contains( "b", "c" ) );
        assertThat( evt.getReorderedRepositoryIds().size(), equalTo( 0 ) );
    }

    @Test
    public void testMultiChange2()
    {
        final List<String> oldMembers = Arrays.asList( "a", "b", "c", "d" );
        final List<String> newMembers = Arrays.asList( "f", "e", "c", "a" );
        // we removed "b" and "d", added "f", "e", and reordered "ac" to "ca"
        final RepositoryGroupMembersChangedEvent evt = createEvent( oldMembers, newMembers );
        assertThat( evt.getMemberChangeSet().size(), equalTo( 3 ) );
        assertThat( evt.getMemberChangeSet(),
            containsInAnyOrder( MemberChange.MEMBER_ADDED, MemberChange.MEMBER_REMOVED, MemberChange.MEMBER_REORDERED ) );
        assertThat( evt.getAddedRepositoryIds(), contains( "f", "e" ) );
        assertThat( evt.getRemovedRepositoryIds(), contains( "b", "d" ) );
        assertThat( evt.getReorderedRepositoryIds(), contains( "c", "a" ) );
    }

}
