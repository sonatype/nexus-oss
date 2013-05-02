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
import org.apache.shiro.authc.credential.Md5CredentialsMatcher;
import org.apache.shiro.authc.credential.Sha1CredentialsMatcher;

/**
 * For users migrated from Artifactory, their password is encrypted with md5, while users' password is enrypted with
 * sha1, so here we use first try sha1, then md5, to meet both requirements.
 * 
 * @author Juven Xu
 */
public class Sha1ThenMd5CredentialsMatcher
    implements CredentialsMatcher
{
    private CredentialsMatcher sha1Matcher = new Sha1CredentialsMatcher();

    private CredentialsMatcher md5Matcher = new Md5CredentialsMatcher();

    public boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        if ( sha1Matcher.doCredentialsMatch( token, info ) )
        {
            return true;
        }

        if ( md5Matcher.doCredentialsMatch( token, info ) )
        {
            return true;
        }

        return false;
    }
}
