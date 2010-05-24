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
