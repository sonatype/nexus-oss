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
package org.sonatype.nexus.repository.yum.testsuite;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientResponseException;
import org.sonatype.nexus.client.core.subsystem.security.User;
import org.sonatype.nexus.client.core.subsystem.security.Users;
import org.sonatype.nexus.repository.yum.client.Yum;

public class SecurityIT
    extends YumRepositoryITSupport
{

    private static final String ANOTHER_VERSION = "4.3.1";

    private static final String VERSION = "1.2.3";

    private static final String REPO = "releases";

    private static final String PASSWORD = "yum123";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public SecurityIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void shouldNotHaveReadAccessToAliasesForAnonymous()
        throws Exception
    {
        final String alias = uniqueName();
        yum().createOrUpdateAlias( REPO, alias, VERSION );

        thrown.expect( NexusClientResponseException.class );
        thrown.expectMessage( "401" );
        createNexusClientForAnonymous( nexus() ).getSubsystem( Yum.class ).getAliasVersion( REPO, alias );
    }

    @Test
    public void shouldNotCreateAliasForAnonymous()
        throws Exception
    {
        thrown.expect( NexusClientResponseException.class );
        thrown.expectMessage( "401" );
        createNexusClientForAnonymous( nexus() ).getSubsystem( Yum.class )
            .createOrUpdateAlias( REPO, uniqueName(), VERSION );
    }

    @Test
    public void shouldNotHaveUpdateAccessToAliasesForAnonymous()
        throws Exception
    {
        final String alias = uniqueName();
        yum().createOrUpdateAlias( REPO, alias, VERSION );
        thrown.expect( NexusClientResponseException.class );
        thrown.expectMessage( "401" );
        createNexusClientForAnonymous( nexus() ).getSubsystem( Yum.class )
            .createOrUpdateAlias( REPO, alias, "3.2.1" );
    }

    @Test
    public void shouldAllowAccessForYumAdmin()
        throws Exception
    {
        final User user = givenYumAdminUser();
        final Yum yum = createNexusClient( nexus(), user.id(), PASSWORD ).getSubsystem( Yum.class );
        final String alias = uniqueName();
        yum.createOrUpdateAlias( REPO, alias, VERSION );
        assertThat( yum.getAliasVersion( REPO, alias ), is( VERSION ) );
        yum.createOrUpdateAlias( REPO, alias, ANOTHER_VERSION );
        assertThat( yum.getAliasVersion( REPO, alias ), is( ANOTHER_VERSION ) );
    }

    private User givenYumAdminUser()
    {
        final String username = testMethodName();

        return client().getSubsystem( Users.class ).create( username )
            .withEmail( username + "@sonatype.org" )
            .withFirstName( "bar" )
            .withLastName( "foo" )
            .withPassword( PASSWORD )
            .withRole( "anonymous" )
            .withRole( "nexus-yum-admin" )
            .save();
    }

    public static String uniqueName()
    {
        return "repo_" + new SimpleDateFormat( "yyyyMMdd_HHmmss_SSS" ).format( new Date() );
    }

}
