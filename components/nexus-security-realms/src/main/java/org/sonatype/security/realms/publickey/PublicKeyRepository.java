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
import java.util.Set;

/**
 * Repository for obtaining each user account's {@link java.security.PublicKey}s. An implementation of this interface is
 * required by the {@link com.sonatype.sshjgit.core.shiro.publickey.PublicKeyAuthenticatingRealm}.
 * 
 * @author hugo@josefson.org
 */
public interface PublicKeyRepository
{

    /**
     * Add a public key to a principal.
     * 
     * @param principal
     * @param publicKey
     */
    public void addPublicKey( Object principal, PublicKey publicKey );

    /**
     * Add a Set of public keys to a principal.
     * 
     * @param principal
     * @param publicKeys
     */
    public void addPublicKeys( Object principal, Set<PublicKey> publicKeys );

    /**
     * Remove a public key from a principal.
     * 
     * @param principal
     * @param publicKey
     */
    public void removePublicKey( Object principal, PublicKey publicKey );

    /**
     * Retrieves an account's {@link java.security.PublicKey}s.
     * 
     * @param principal the principal to look up.
     * @return a set of keys with which the account is allowed to authenticate. never {@code null}.
     */
    Set<PublicKey> getPublicKeys( Object principal );

    /**
     * Checks to see if this repository has an account with the supplied principal.
     * 
     * @param principal the principal to look for.
     * @return {@code true} is the account is known, {@code false} otherwise.
     */
    boolean hasAccount( Object principal );

}
