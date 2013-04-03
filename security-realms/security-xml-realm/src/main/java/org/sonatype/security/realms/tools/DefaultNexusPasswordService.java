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
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.format.HexFormat;
import org.sonatype.security.configuration.SecurityConfigurationManager;

@Singleton
@Typed( PasswordService.class )
@Named( "default" )
public class DefaultNexusPasswordService
    implements HashingPasswordService
{   
    private static final String DEFAULT_HASH_ALGORITHM = "SHA-512";
    
    /**
     * Provides access to password hashing policy
     * Currently only provides hash iterations, but could be extended
     * at some point to include hashing algorithm, salt policy, etc
     */
    private final SecurityConfigurationManager securityConfiguration;
    
    private final DefaultPasswordService passwordService;
    
    private final DefaultPasswordService legacyPasswordService;
    
    @Inject
    public DefaultNexusPasswordService(SecurityConfigurationManager securityConfiguration)
    {
        this.securityConfiguration = securityConfiguration;
        this.passwordService = new DefaultPasswordService();

        //Create and set a hash service according to our hashing policies 
        DefaultHashService hashService = new DefaultHashService();
        hashService.setHashAlgorithmName(DEFAULT_HASH_ALGORITHM);
        hashService.setHashIterations(this.securityConfiguration.getHashIterations());
        hashService.setGeneratePublicSalt(true);
        this.passwordService.setHashService(hashService);
        
        this.legacyPasswordService = new DefaultPasswordService();
        DefaultHashService legacyHashService = new DefaultHashService();
        legacyHashService.setHashAlgorithmName("SHA-1");
        legacyHashService.setHashIterations(1);
        legacyHashService.setGeneratePublicSalt(false);
        this.legacyPasswordService.setHashService(legacyHashService);
        this.legacyPasswordService.setHashFormat(new HexFormat());
        
    }

    @Override
    public String encryptPassword(Object plaintextPassword)
        throws IllegalArgumentException
    {
        return this.passwordService.encryptPassword(plaintextPassword);
    }

    @Override
    public boolean passwordsMatch(Object submittedPlaintext, String encrypted)
    {
        return this.legacyPasswordService.passwordsMatch(submittedPlaintext,  encrypted);
    }

    @Override
    public Hash hashPassword(Object plaintext)
        throws IllegalArgumentException
    {
        return this.passwordService.hashPassword(plaintext);
    }

    @Override
    public boolean passwordsMatch(Object plaintext, Hash savedPasswordHash)
    {
        return this.passwordService.passwordsMatch(plaintext,  savedPasswordHash);
    }

}
