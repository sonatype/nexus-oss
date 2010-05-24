package org.sonatype.security.realms.publickey;

import org.apache.shiro.authc.AuthenticationToken;

import java.security.PublicKey;

/**
 * {@link AuthenticationToken} for a {@link PublicKey}.
 *
 * @author hugo@josefson.org
 */
public class PublicKeyAuthenticationToken implements AuthenticationToken {
    
    private static final long serialVersionUID = -784273150987377079L;
    private final Object principal;
    private final PublicKey key;

    public PublicKeyAuthenticationToken(Object principal, PublicKey key) {
        this.principal = principal;
        this.key = key;
    }

    public Object getPrincipal() {
        return principal;
    }

    public PublicKey getCredentials() {
        return key;
    }
}
