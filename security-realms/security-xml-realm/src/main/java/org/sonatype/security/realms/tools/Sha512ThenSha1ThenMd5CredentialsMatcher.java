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
package org.sonatype.security.realms.tools;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

/**
 * Credentials matcher that first tries to match using SHA-512. If that fails, it falls back to the previous
 * matcher that was used, which first tries to match using SHA-1, and if that fails, tries MD5
 * 
 * @author Steve Carlucci
 */
public class Sha512ThenSha1ThenMd5CredentialsMatcher
    implements CredentialsMatcher
{
    private HashedCredentialsMatcher sha512Matcher = new HashedCredentialsMatcher("SHA-512");

    private CredentialsMatcher sha1Md5Matcher = new Sha1ThenMd5CredentialsMatcher();
    
    public Sha512ThenSha1ThenMd5CredentialsMatcher(int hashIterations) {    	
    	sha512Matcher.setHashIterations(hashIterations);
    }

    public boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        if ( sha512Matcher.doCredentialsMatch( token, info) )
        {
            return true;
        }

        if ( sha1Md5Matcher.doCredentialsMatch( token, info ) )
        {
            return true;
        }

        return false;
    }
}
