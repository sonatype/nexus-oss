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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

/**
 * Matches authentication tokens which are {@link java.security.PublicKey}.
 * 
 * @author hugo@josefson.org
 */
class PublicKeyCredentialsMatcher
    implements CredentialsMatcher
{

    public boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        PublicKeyWithEquals tokenKey = getTokenKey( token );
        Collection<PublicKeyWithEquals> infoKeys = getInfoKeys( info );
        for ( PublicKeyWithEquals infoKey : infoKeys )
        {
            if ( infoKey.equals( tokenKey ) )
            {
                return true;
            }
        }
        return false;
    }

    protected PublicKeyWithEquals getTokenKey( AuthenticationToken token )
    {
        final PublicKeyAuthenticationToken publicKeyAuthentictionToken = (PublicKeyAuthenticationToken) token;
        return new PublicKeyWithEquals( publicKeyAuthentictionToken.getCredentials() );
    }

    protected Collection<PublicKeyWithEquals> getInfoKeys( AuthenticationInfo info )
    {
        // TODO: check types so they are sure to be PublicKey
        final Set<PublicKeyWithEquals> result = new HashSet<PublicKeyWithEquals>();
        final Object credentials = info.getCredentials();
        if ( Collection.class.isAssignableFrom( credentials.getClass() ) )
        {
            Collection<PublicKey> credentialsCollection = (Collection<PublicKey>) credentials;
            for ( PublicKey publicKey : credentialsCollection )
            {
                result.add( new PublicKeyWithEquals( publicKey ) );
            }
        }
        else
        {
            result.add( new PublicKeyWithEquals( (PublicKey) credentials ) );
        }
        return result;
    }
}
