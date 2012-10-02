/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.yum.plugin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.plugins.yum.NameUtil.uniqueName;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.security.Users;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.YumClient;
import org.sonatype.security.rest.model.UserResource;

import com.sun.jersey.api.client.UniformInterfaceException;

public class SecurityIT
    extends AbstractIntegrationTestCase
{
    private static final String ANOTHER_VERSION = "4.3.1";

    private static final String VERSION = "1.2.3";

    private static final String REPO = "releases";

    private static final String PASSWORD = "yum123";

    @Test( expected = UniformInterfaceException.class )
    public void shouldNotHaveReadAccessToAliasesForAnonymous()
        throws Exception
    {
        final String alias = uniqueName();
        yum().createOrUpdateAlias( REPO, alias, VERSION );
        createNexusClientForAnonymous( nexus() ).getSubsystem( YumClient.class ).getAliasVersion( REPO, alias );
    }

    @Test( expected = UniformInterfaceException.class )
    public void shouldNotCreateAliasForAnonymous()
        throws Exception
    {
        createNexusClientForAnonymous( nexus() ).getSubsystem( YumClient.class ).createOrUpdateAlias( REPO,
            uniqueName(), VERSION );
    }

    @Test( expected = UniformInterfaceException.class )
    public void shouldNotHaveUpdateAccessToAliasesForAnonymous()
        throws Exception
    {
        final String alias = uniqueName();
        yum().createOrUpdateAlias( REPO, alias, VERSION );
        createNexusClientForAnonymous( nexus() ).getSubsystem( YumClient.class ).createOrUpdateAlias( REPO, alias,
            "3.2.1" );
    }

    @Test
    public void shouldAllowAccessForYumAdmin()
        throws Exception
    {
        final UserResource user = givenYumAdminUser();
        final YumClient yum = createNexusClient( nexus(), user.getUserId(), PASSWORD ).getSubsystem( YumClient.class );
        final String alias = uniqueName();
        yum.createOrUpdateAlias( REPO, alias, VERSION );
        assertThat( yum.getAliasVersion( REPO, alias ), is( VERSION ) );
        yum.createOrUpdateAlias( REPO, alias, ANOTHER_VERSION );
        assertThat( yum.getAliasVersion( REPO, alias ), is( ANOTHER_VERSION ) );
    }

    private UserResource givenYumAdminUser()
    {
        final String username = uniqueName();
        final Users users = client().getSubsystem( Users.class );
        final UserResource user = new UserResource();

        user.setUserId( username );
        user.setEmail( username + "@sonatype.org" );
        user.setFirstName( "bar" );
        user.setLastName( "foo" );
        user.setPassword( PASSWORD );
        user.setStatus( "active" );
        user.addRole( "anonymous" );
        user.addRole( "nexus-yum-admin" );
        return users.create( user );
    }
}
