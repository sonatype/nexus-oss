/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.publickey;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

public class PublicKeyRepositoryTest
{

    /**
     * @return the PublicKeyRepository under test.
     */
    protected PublicKeyRepository getPublicKeyRepository()
    {
        return new SimplePublicKeyRepository();
    }

    @Test
    public void testAddPublicKey()
    {
        PublicKeyRepository keyRepo = this.getPublicKeyRepository();

        PublicKey key1 = new MockPublicKey( "key1" );
        PublicKey key2 = new MockPublicKey( "key2" );
        PublicKey invalidKey = new MockPublicKey( "invalidKey" );

        keyRepo.addPublicKey( "user", key1 );
        keyRepo.addPublicKey( "user", key2 );

        Set<PublicKey> keys = keyRepo.getPublicKeys( "user" );
        Assert.assertTrue( keys.contains( key1 ) );
        Assert.assertTrue( keys.contains( key2 ) );
        Assert.assertFalse( keys.contains( invalidKey ) );
        Assert.assertEquals( 2, keys.size() );
    }

    @Test
    public void testAddPublicKeys()
    {
        PublicKeyRepository keyRepo = this.getPublicKeyRepository();

        PublicKey key1 = new MockPublicKey( "key1" );
        PublicKey key2 = new MockPublicKey( "key2" );
        PublicKey key3 = new MockPublicKey( "key3" );

        Set<PublicKey> userKeys = new HashSet<PublicKey>();
        userKeys.add( key1 );
        userKeys.add( key2 );

        keyRepo.addPublicKeys( "user", userKeys );
        keyRepo.addPublicKey( "user", key3 );

        Set<PublicKey> keys = keyRepo.getPublicKeys( "user" );
        Assert.assertTrue( keys.contains( key1 ) );
        Assert.assertTrue( keys.contains( key2 ) );
        Assert.assertTrue( keys.contains( key3 ) );
        Assert.assertEquals( 3, keys.size() );
    }

    @Test
    public void testRemovePublicKey()
    {
        PublicKeyRepository keyRepo = this.getPublicKeyRepository();

        PublicKey key1 = new MockPublicKey( "key1" );
        PublicKey key2 = new MockPublicKey( "key2" );
        PublicKey key3 = new MockPublicKey( "key3" );

        keyRepo.addPublicKey( "user", key1 );
        keyRepo.addPublicKey( "user", key2 );
        keyRepo.addPublicKey( "user", key3 );

        // now remove key2
        keyRepo.removePublicKey( "user", key2 );

        Set<PublicKey> keys = keyRepo.getPublicKeys( "user" );
        Assert.assertTrue( keys.contains( key1 ) );
        Assert.assertFalse( keys.contains( key2 ) ); // is not here
        Assert.assertTrue( keys.contains( key3 ) );
        Assert.assertEquals( 2, keys.size() );
    }

    @Test
    public void testHasAccount()
    {
        PublicKeyRepository keyRepo = this.getPublicKeyRepository();
        keyRepo.addPublicKey( "user1", new MockPublicKey( "key1" ) );
        keyRepo.addPublicKey( "user2", new MockPublicKey( "key2" ) );

        Assert.assertTrue( keyRepo.hasAccount( "user1" ) );
        Assert.assertTrue( keyRepo.hasAccount( "user2" ) );
        Assert.assertFalse( keyRepo.hasAccount( "user3" ) );
    }
}
