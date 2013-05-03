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

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.format.HexFormat;

/*
 * PasswordService for handling legacy passwords (SHA-1 and MD5)
 * 
 * @since 3.1
 */
@Singleton
@Typed( PasswordService.class )
@Named( "legacy" )
public class LegacyNexusPasswordService
    implements PasswordService
{
    DefaultPasswordService sha1PasswordService;
    
    DefaultPasswordService md5PasswordService;
    
    public LegacyNexusPasswordService()
    {
        //Initialize and configure sha1 password service
        this.sha1PasswordService = new DefaultPasswordService();
        DefaultHashService sha1HashService = new DefaultHashService();
        sha1HashService.setHashAlgorithmName("SHA-1");
        sha1HashService.setHashIterations(1);
        sha1HashService.setGeneratePublicSalt(false);
        this.sha1PasswordService.setHashService(sha1HashService);
        this.sha1PasswordService.setHashFormat(new HexFormat());
        
        //Initialize and configure md5 password service
        this.md5PasswordService = new DefaultPasswordService();
        DefaultHashService md5HashService = new DefaultHashService();
        md5HashService.setHashAlgorithmName("MD5");
        md5HashService.setHashIterations(1);
        md5HashService.setGeneratePublicSalt(false);
        this.md5PasswordService.setHashService(md5HashService);
        this.md5PasswordService.setHashFormat(new HexFormat());
    }

    @Override
    public String encryptPassword(Object plaintextPassword)
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException("Not supported");        
    }

    @Override
    public boolean passwordsMatch(Object submittedPlaintext, String encrypted)
    {
        //Legacy passwords can be hashed with sha-1 or md5, check both
        
        if(this.sha1PasswordService.passwordsMatch(submittedPlaintext, encrypted))
        {
            return true;
        }
        
        if(this.md5PasswordService.passwordsMatch(submittedPlaintext, encrypted))
        {
            return true;
        }
        
        return false;
    }
}
