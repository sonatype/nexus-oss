/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;

import org.junit.Test;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.security.Privilege;
import org.sonatype.nexus.client.core.subsystem.security.Privileges;
import org.sonatype.nexus.client.core.subsystem.security.Role;
import org.sonatype.nexus.client.core.subsystem.security.Roles;
import org.sonatype.nexus.client.core.subsystem.security.User;
import org.sonatype.nexus.client.core.subsystem.security.Users;
import org.sonatype.nexus.client.core.subsystem.targets.RepositoryTarget;
import org.sonatype.nexus.client.core.subsystem.targets.RepositoryTargets;

public class ClientSecurityIT
    extends ClientITSupport
{

    public ClientSecurityIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void getRoles()
    {
        final Collection<Role> roles = roles().get();
        assertThat( roles, is( not( empty() ) ) );
    }

    @Test
    public void getRole()
    {
        final Role role = roles().get( "ui-search" );
        assertThat( role, is( notNullValue() ) );
        assertThat( role.id(), is( "ui-search" ) );
    }

    @Test
    public void createRole()
    {
        final String roleId = testName.getMethodName();
        roles().create( roleId )
            .withName( roleId )
            .withPrivilege( "19" )
            .save();
        final Role role = roles().get( roleId );
        assertThat( role, is( notNullValue() ) );
        assertThat( role.id(), is( roleId ) );
        assertThat( role.name(), is( roleId ) );
    }

    @Test
    public void updateRole()
    {
        final String roleId = testName.getMethodName();
        roles().create( roleId )
            .withName( roleId )
            .withPrivilege( "19" )
            .save()
            .withName( roleId + "Bar" )
            .save();
        final Role role = roles().get( roleId );
        assertThat( role, is( notNullValue() ) );
        assertThat( role.name(), is( roleId + "Bar" ) );
    }

    @Test
    public void deleteRole()
    {
        final String roleId = testName.getMethodName();
        final Role role = roles().create( roleId )
            .withName( roleId )
            .withPrivilege( "19" )
            .save();
        role.remove();
    }

    @Test
    public void refreshRole()
    {
        final String roleId = testName.getMethodName();
        Role role = roles().create( roleId )
            .withName( roleId )
            .withPrivilege( "19" )
            .save()
            .withName( roleId + "Bar" )
            .refresh();
        assertThat( role.id(), is( roleId ) );
        role = roles().get( roleId );
        assertThat( role, is( notNullValue() ) );
        assertThat( role.name(), is( roleId ) );
    }

    @Test
    public void getUsers()
    {
        final Collection<User> users = users().get();
        assertThat( users, is( not( empty() ) ) );
    }

    @Test
    public void createUser()
    {
        final String username = testMethodName();
        users().create( username )
            .withEmail( username + "@sonatype.org" )
            .withFirstName( "bar" )
            .withLastName( "foo" )
            .withPassword( "super secret" )
            .withRole( "anonymous" )
            .save();

        final User user = users().get( username );
        assertThat( user, is( notNullValue() ) );
        assertThat( user.firstName(), is( "bar" ) );
    }

    @Test
    public void updateUser()
    {
        final String username = testMethodName();
        users().create( username )
            .withEmail( username + "@sonatype.org" )
            .withFirstName( "bar" )
            .withLastName( "foo" )
            .withPassword( "super secret" )
            .withRole( "anonymous" )
            .save();

        final User user = users().get( username )
            .withFirstName( "Bar the second" )
            .save();

        assertThat( user, is( notNullValue() ) );
        assertThat( user.firstName(), is( "Bar the second" ) );
    }

    @Test
    public void deleteUser()
    {
        final String username = testMethodName();
        final User user = users().create( username )
            .withEmail( username + "@sonatype.org" )
            .withFirstName( "bar" )
            .withLastName( "foo" )
            .withPassword( "super secret" )
            .withRole( "anonymous" )
            .save();
        user.remove();
    }

    @Test
    public void getUser()
    {
        final User user = users().get( "admin" );
        assertThat( user, is( notNullValue() ) );
        assertThat( user.id(), is( "admin" ) );
    }

    @Test
    public void getPrivileges()
    {
        assertThat( privileges().get(), is( not( empty() ) ) );
    }

    @Test
    public void getPrivilege()
    {
        // admin privilege
        final Privilege privilege = privileges().get( "1000" );
        assertThat( privilege, is( not( nullValue() ) ) );
        assertThat( privilege.name(), containsString( "Administrator" ) );
    }

    @Test
    public void createPrivilege()
    {
        final String targetId = createRepoTarget( "createPrivileges" ).id();
        final Privilege saved = privileges().create()
            .withName( "foo" )
            .withDescription( "bar" )
            .withMethods( "read" )
            .withRepositoryGroupId( "public" )
            .withTargetId( targetId )
            .create().iterator().next();


        final Privilege privilege = privileges().get( saved.id() );
        assertThat( privilege, is( notNullValue() ) );
        assertThat( privilege.description(), is( "bar" ) );

        // name is mangled on creation - "$name - ($method)"
        assertThat( privilege.name(), is( saved.name() ) );

        assertThat( privilege.methods(), contains( "read" ) );
        assertThat( privilege.repositoryGroupId(), is( "public" ) );
        assertThat( privilege.targetId(), is( targetId ) );
    }

    @Test( expected = IllegalStateException.class )
    public void refuseCreateAlreadyExistingPrivilege()
    {
        final String targetId = createRepoTarget( "refuseCreatePrivileges" ).id();
        final Privilege saved = privileges().create()
            .withName( "foo" )
            .withDescription( "bar" )
            .withMethods( "read" )
            .withRepositoryGroupId( "public" )
            .withTargetId( targetId )
            .create().iterator().next();

        saved.create();
    }

    @Test( expected = UnsupportedOperationException.class )
    public void unsupportedUpdatePrivilege()
    {
        final String targetId = createRepoTarget( "unsupportedUpdatePrivileges" ).id();
        final Privilege saved = privileges().create()
            .withName( "foo" )
            .withDescription( "bar" )
            .withMethods( "read" )
            .withRepositoryGroupId( "public" )
            .withTargetId( targetId )
            .create().iterator().next();

        saved.save();
    }

    @Test( expected = NexusClientNotFoundException.class )
    public void deletePrivilege()
    {
        final String targetId = createRepoTarget( "deletePrivileges" ).id();
        final Privilege saved = privileges().create()
            .withName( "foo" )
            .withDescription( "bar" )
            .withMethods( "read" )
            .withRepositoryGroupId( "public" )
            .withTargetId( targetId )
            .create().iterator().next();

        saved.remove();

        privileges().get( saved.id() );
    }

    private RepositoryTarget createRepoTarget( final String id )
    {
        return targets().create( id ).withContentClass( "maven2" ).withName( id ).withPatterns(
            "some_pattern" ).save();
    }

    private Roles roles()
    {
        return client().getSubsystem( Roles.class );
    }

    private Users users()
    {
        return client().getSubsystem( Users.class );
    }

    private Privileges privileges()
    {
        return client().getSubsystem( Privileges.class );
    }

    private RepositoryTargets targets()
    {
        return client().getSubsystem( RepositoryTargets.class );
    }
}
