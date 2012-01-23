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
package org.sonatype.nexus.proxy;

import org.junit.Test;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;

public class RepoChecksumPolicyTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    protected M2Repository getRepository()
        throws Exception
    {
        return (M2Repository) getRepositoryRegistry().getRepository( "checksumTestRepo" );
    }

    public StorageFileItem requestWithPolicy( ChecksumPolicy policy, ResourceStoreRequest request )
        throws Exception
    {
        M2Repository repo = getRepository();

        repo.setChecksumPolicy( policy );
        repo.getCurrentCoreConfiguration().commitChanges();

        StorageFileItem item = (StorageFileItem) repo.retrieveItem( request );

        return item;
    }

    @Test
    public void testPolicyIgnore()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.IGNORE;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // IGNORE: the req ignores checksum
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
    }

    @Test
    public void testPolicyWarn()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.WARN;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // WARN: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
    }

    @Test
    public void testPolicyStrictIfExists()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.STRICT_IF_EXISTS;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        try
        {
            file = requestWithPolicy( policy, new ResourceStoreRequest(
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
                false ) );
            checkForFileAndMatchContents( file );
            // STRICT_IF_EXISTS: the req implicitly gets the "best" checksum available implicitly

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
    }

    @Test
    public void testPolicyStrict()
        throws Exception
    {
        ChecksumPolicy policy = ChecksumPolicy.STRICT;

        // it should simply pull all four without problem
        StorageFileItem file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-all/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );

        file = requestWithPolicy( policy, new ResourceStoreRequest(
            "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar",
            false ) );
        checkForFileAndMatchContents( file );
        // STRICT: the req implicitly gets the "best" checksum available implicitly
        assertTrue( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-md5/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        try
        {
            file = requestWithPolicy( policy, new ResourceStoreRequest(
                "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar",
                false ) );
            checkForFileAndMatchContents( file );
            // STRICT: the req implicitly gets the "best" checksum available implicitly

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-none/activemq-core/1.2/activemq-core-1.2.jar.md5", true ) ) );

        try
        {
            file = requestWithPolicy( policy, new ResourceStoreRequest(
                "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar",
                false ) );
            checkForFileAndMatchContents( file );
            // STRICT: the req implicitly gets the "best" checksum available implicitly

            fail();
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }
        assertFalse( getRepository().getLocalStorage().containsItem(
            getRepository(),
            new ResourceStoreRequest( "/activemq-with-wrong-sha1/activemq-core/1.2/activemq-core-1.2.jar.sha1", true ) ) );
    }
}
