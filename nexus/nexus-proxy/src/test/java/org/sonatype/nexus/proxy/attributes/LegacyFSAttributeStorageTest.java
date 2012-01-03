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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.internal.DefaultAttributes;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * AttributeStorage implementation driven by XStream.
 *
 * @author cstamas
 */
public class LegacyFSAttributeStorageTest
    extends AbstractNexusTestEnvironment
{

    protected LegacyFSAttributeStorage attributeStorage;

    protected File proxyAttributesDirectory;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        proxyAttributesDirectory = getTestFile( "target/test-classes/nexus4660" );

        ApplicationConfiguration applicationConfiguration = Mockito.mock( ApplicationConfiguration.class );
        Mockito.when( applicationConfiguration.getWorkingDirectory( "proxy/attributes", false ) ).thenReturn(
            proxyAttributesDirectory );

        attributeStorage = new LegacyFSAttributeStorage( applicationConfiguration );

        attributeStorage.initializeWorkingDirectory();
    }

    protected RepositoryItemUid createUid( final String path )
    {
        final RepositoryItemUidLock fakeLock = new RepositoryItemUidLock()
        {
            @Override
            public void lock( final Action action )
            {
            }

            @Override
            public void unlock()
            {
            }

            @Override
            public boolean hasLocksHeld()
            {
                return false;
            }
        };
        final Repository repository = Mockito.mock( Repository.class );
        Mockito.when( repository.getId() ).thenReturn( "test" );
        final RepositoryItemUid uid = Mockito.mock( RepositoryItemUid.class );
        Mockito.when( uid.getRepository() ).thenReturn( repository );
        Mockito.when( uid.getPath() ).thenReturn( path );
        Mockito.when( uid.getAttributeLock() ).thenReturn( fakeLock );

        return uid;
    }

    @Test
    public void testGetAttributes()
    {
        final RepositoryItemUid uid = createUid( "classworlds/classworlds/1.1/classworlds-1.1.pom" );
        final Attributes attributes = attributeStorage.getAttributes( uid );
        // must be present
        MatcherAssert.assertThat( attributes, Matchers.notNullValue() );
        // do some random checks that conent is okay
        MatcherAssert.assertThat( attributes.getGeneration(), Matchers.equalTo( 4 ) );
        MatcherAssert.assertThat( attributes.get( StorageFileItem.DIGEST_SHA1_KEY ),
                                  Matchers.equalTo( "4703c4199028094698c222c17afea6dcd9f04999" ) );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void testPutAttributes()
    {
        final RepositoryItemUid uid = createUid( "/some/path" );
        attributeStorage.putAttributes( uid, new DefaultAttributes() );
    }

    @Test
    public void testDeleteAttributes()
    {
        final String path = "classworlds/classworlds/1.1/classworlds-1.1.pom";
        final RepositoryItemUid uid = createUid( path );
        MatcherAssert.assertThat( attributeStorage.deleteAttributes( uid ), Matchers.is( true ) );
        MatcherAssert.assertThat( new File( proxyAttributesDirectory, path ).exists(), Matchers.is( false ) );
    }
}
