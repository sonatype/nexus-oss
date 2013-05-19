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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@code PublicKeyRepository} which stores its accounts in memory.
 * 
 * @author hugo@josefson.org
 */
public class SimplePublicKeyRepository
    implements PublicKeyRepository
{

    /**
     * principal-to-publickeys. note that you must use {@link #accountsLock} when touching this.
     */
    protected final Map<Object, Set<PublicKey>> accounts = new HashMap<Object, Set<PublicKey>>();

    /** lock for {@link #accounts} */
    protected final ReentrantReadWriteLock accountsLock = new ReentrantReadWriteLock();

    /**
     * Adds one publicKey with which a specific principal will be allowed to authenticate.
     * 
     * @param principal the account's principal
     * @param publicKey the publicKey this principal will be allowed to authenticate with
     * @see #addPublicKeys(Object, java.util.Set)
     */
    public void addPublicKey( Object principal, PublicKey publicKey )
    {
        final HashSet<PublicKey> publicKeys = new HashSet<PublicKey>( 1 );
        publicKeys.add( publicKey );
        addPublicKeys( principal, publicKeys );
    }

    /**
     * Adds a set of publicKeys with which a specific principal will be allowed to authenticate.
     * 
     * @param principal the account's principal
     * @param publicKeys the publicKeys this principal is allowed to authenticate with
     */
    public void addPublicKeys( Object principal, Set<PublicKey> publicKeys )
    {
        accountsLock.writeLock().lock();
        try
        {
            if ( hasAccount( principal ) )
            {
                accounts.get( principal ).addAll( publicKeys );
            }
            else
            {
                accounts.put( principal, new HashSet<PublicKey>( publicKeys ) );
            }
        }
        finally
        {
            accountsLock.writeLock().unlock();
        }
    }

    /**
     * Removes a {@code PublicKey} from the specified account.
     * 
     * @param principal which account to remove the publicKey from
     * @param publicKey the ssh public key
     */
    public void removePublicKey( Object principal, PublicKey publicKey )
    {
        accountsLock.writeLock().lock(); // start with a write lock, because we cannot upgrade the lock (only
                                         // down-grade)
        try
        {
            if ( hasAccount( principal ) )
            {
                accounts.get( principal ).remove( publicKey );
            }
            else
            {
                // good already
            }
        }
        finally
        {
            accountsLock.writeLock().unlock();
        }
    }

    public Set<PublicKey> getPublicKeys( Object principal )
    {
        accountsLock.readLock().lock();
        try
        {
            final Set<PublicKey> publicKeys = accounts.get( principal );
            if ( publicKeys != null )
            {
                return new HashSet<PublicKey>( publicKeys );
            }
            else
            {
                return Collections.emptySet();
            }
        }
        finally
        {
            accountsLock.readLock().unlock();
        }
    }

    public boolean hasAccount( Object principal )
    {
        accountsLock.readLock().lock();
        try
        {
            return accounts.containsKey( principal );
        }
        finally
        {
            accountsLock.readLock().unlock();
        }
    }

}
