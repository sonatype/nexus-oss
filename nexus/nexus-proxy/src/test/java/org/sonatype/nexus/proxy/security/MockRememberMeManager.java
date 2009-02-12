package org.sonatype.nexus.proxy.security;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.RememberMeManager;

public class MockRememberMeManager
    implements RememberMeManager
{

    public PrincipalCollection getRememberedPrincipals()
    {
        return null;
    }

    public void onFailedLogin( AuthenticationToken arg0, AuthenticationException arg1 )
    {
    }

    public void onLogout( PrincipalCollection arg0 )
    {
    }

    public void onSuccessfulLogin( AuthenticationToken arg0, AuthenticationInfo arg1 )
    {
    }

}
