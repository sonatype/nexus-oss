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
package org.sonatype.nexus.testsuite.unpack;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonatype.nexus.client.core.exception.NexusClientAccessForbiddenException;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.security.User;

/**
 * @since 2.5.1
 */
public class UnpackAuthorizationIT
    extends UnpackITSupport
{

    private static final String PASSWORD = "secret";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public UnpackAuthorizationIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    /**
     * Verify that uploading a zip, using a user that does not have the "unpack" role (privilege "content-compressed"),
     * will fail with 403.
     */
    @Test
    public void uploadUsingUserWithoutUnpackPrivilege()
        throws Exception
    {
        final User user = createUser();

        final MavenHostedRepository repository = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest()
        ).save();

        thrown.expect( NexusClientAccessForbiddenException.class );
        upload(
            createNexusClient( nexus(), user.id(), PASSWORD ),
            repository.id(),
            testData().resolveFile( "bundle.zip" )
        );
    }

    /**
     * Verify that uploading a zip, using a user that has the "unpack" role (privilege "content-compressed"),
     * will succeed.
     */
    @Test
    public void uploadUsingUserWithUnpackPrivilege()
        throws Exception
    {
        final User user = createUser().withRole( "unpack" ).save();

        final MavenHostedRepository repository = repositories().create(
            MavenHostedRepository.class, repositoryIdForTest()
        ).save();

        upload(
            createNexusClient( nexus(), user.id(), PASSWORD ),
            repository.id(),
            testData().resolveFile( "bundle.zip" )
        );
    }

    private User createUser()
    {
        return users().create( uniqueName( "unpack" ) )
            .withFirstName( testMethodName() )
            .withLastName( "Bithub" )
            .withEmail( testMethodName() + "@sonatype.com" )
            .withPassword( PASSWORD )
            .withRole( "nx-deployment" )
            .withRole( "repository-any-full" )
            .save();
    }

}
