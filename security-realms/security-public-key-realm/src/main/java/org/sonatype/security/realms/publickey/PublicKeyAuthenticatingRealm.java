package org.sonatype.security.realms.publickey;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Shiro {@link org.apache.shiro.realm.Realm} for authenticating {@link java.security.PublicKey}s.
 * Authorization is delegated to a Shiro
 * {@link org.apache.shiro.authz.Authorizer}, which can be a different
 * {@link org.apache.shiro.realm.Realm}.
 *
 * Implement a {@link PublicKeyRepository} in which you consult your own
 * accounts backend, or use the
 * {@link com.sonatype.sshjgit.core.shiro.publickey.SimplePublicKeyRepository}
 * for testing purposes.
 *
 * @see com.sonatype.sshjgit.core.shiro.publickey.PublicKeyRepository
 * @see org.apache.shiro.realm.Realm
 *
 * @author hugo@josefson.org
 * @author Brian Demers
 */
public class PublicKeyAuthenticatingRealm extends AuthorizingRealm {
    protected static final Class<PublicKeyAuthenticationToken> AUTHENTICATION_TOKEN_CLASS = PublicKeyAuthenticationToken.class;
    protected PublicKeyRepository publicKeyRepository;

    /**
     * Default constructor needed for injection. 
     */
    public PublicKeyAuthenticatingRealm()
    {
        setAuthenticationTokenClass(AUTHENTICATION_TOKEN_CLASS);
        setCredentialsMatcher(new PublicKeyCredentialsMatcher());
    }
    
    /**
     * Constructs this realm, accepting a {@code PublicKeyRepository} from which
     * all keys will be fetched, and an {@code Authorizer} to which all
     * authorization will be delegated.
     *
     * @param publicKeyRepository public keys will be looked up from this.
     * @param authorizer all authorization will be delegated to this. can be
     * for example another {@link org.apache.shiro.realm.Realm}.
     */
    public PublicKeyAuthenticatingRealm(PublicKeyRepository publicKeyRepository) {
        this();
        this.publicKeyRepository = publicKeyRepository;
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        final Object principal = token.getPrincipal();

        if ( !publicKeyRepository.hasAccount(principal)){
            return null;
        }

        return new SimpleAuthenticationInfo(
                principal,
                publicKeyRepository.getPublicKeys(principal),
                getName()
        );
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        // No Authorization, just Authentication
        return null;
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        // only support PublicKeyAuthenticationToken
        return PublicKeyAuthenticationToken.class.isInstance( token );
    }

    public void setPublicKeyRepository( PublicKeyRepository publicKeyRepository )
    {
        this.publicKeyRepository = publicKeyRepository;
    }

 
}
