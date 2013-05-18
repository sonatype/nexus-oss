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
package org.sonatype.security.authentication;

import java.util.Collection;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Authenticator will only try to authenticate with each realm. The first successful AuthenticationInfo found will
 * be returned and other realms will not be queried. <BR/>
 * <BR/>
 * This makes for the performance short comings when using the {@link ModularRealmAuthenticator} and
 * {@link FirstSuccessfulAuthenticationStrategy} where all the realms will be queried, but only the first success is
 * returned.
 * 
 * @author Brian Demers
 * @see ModularRealmAuthenticator
 * @see FirstSuccessfulAuthenticationStrategy
 */
public class FirstSuccessfulModularRealmAuthenticator
    extends ModularRealmAuthenticator
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Override
    protected AuthenticationInfo doMultiRealmAuthentication( Collection<Realm> realms, AuthenticationToken token )
    {
        logger.trace( "Iterating through [" + realms.size() + "] realms for PAM authentication" );

        for ( Realm realm : realms )
        {
            // check if the realm supports this token
            if ( realm.supports( token ) )
            {
                if ( logger.isTraceEnabled() )
                {
                    logger.trace( "Attempting to authenticate token [" + token + "] " + "using realm of type [" + realm
                        + "]" );
                }

                try
                {
                    // try to login
                    AuthenticationInfo info = realm.getAuthenticationInfo( token );
                    // just make sure are ducks are in a row
                    // return the first successful login.
                    if ( info != null )
                    {
                        return info;
                    }
                    else if ( logger.isTraceEnabled() )
                    {
                        logger.trace( "Realm [" + realm + "] returned null when authenticating token " + "[" + token
                            + "]" );
                    }
                }
                catch ( Throwable t )
                {
                    if ( logger.isTraceEnabled() )
                    {
                        String msg =
                            "Realm [" + realm + "] threw an exception during a multi-realm authentication attempt:";
                        logger.trace( msg, t );
                    }
                }
            }
            else
            {
                if ( logger.isTraceEnabled() )
                {
                    logger.trace( "Realm of type [" + realm + "] does not support token " + "[" + token
                        + "].  Skipping realm." );
                }
            }
        }
        throw new org.apache.shiro.authc.AuthenticationException( "Authentication token of type [" + token.getClass()
            + "] " + "could not be authenticated by any configured realms.  Please ensure that at least one realm can "
            + "authenticate these tokens." );
    }
}
