package org.sonatype.security.realms.publickey;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

import java.security.PublicKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Matches authentication tokens which are {@link java.security.PublicKey}.
 *
 * @author hugo@josefson.org
 */
class PublicKeyCredentialsMatcher implements CredentialsMatcher {

    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        PublicKeyWithEquals tokenKey = getTokenKey(token);
        Collection<PublicKeyWithEquals> infoKeys = getInfoKeys(info);
        for (PublicKeyWithEquals infoKey : infoKeys) {
            if (infoKey.equals(tokenKey)){
                return true;
            }
        }
        return false;
    }


    protected PublicKeyWithEquals getTokenKey(AuthenticationToken token) {
        final PublicKeyAuthenticationToken publicKeyAuthentictionToken = (PublicKeyAuthenticationToken) token;
        return new PublicKeyWithEquals(publicKeyAuthentictionToken.getCredentials());
    }

    protected Collection<PublicKeyWithEquals> getInfoKeys(AuthenticationInfo info) {
        // TODO: check types so they are sure to be PublicKey
        final Set<PublicKeyWithEquals> result = new HashSet<PublicKeyWithEquals>();
        final Object credentials = info.getCredentials();
        if (Collection.class.isAssignableFrom(credentials.getClass())){
            Collection<PublicKey> credentialsCollection = (Collection<PublicKey>) credentials;
            for (PublicKey publicKey : credentialsCollection) {
                result.add(new PublicKeyWithEquals(publicKey));
            }
        }else{
            result.add(new PublicKeyWithEquals((PublicKey) credentials));
        }
        return result;
    }
}
