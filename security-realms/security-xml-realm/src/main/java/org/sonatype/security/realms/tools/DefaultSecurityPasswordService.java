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

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Hash;
import org.sonatype.security.configuration.SecurityConfigurationManager;

/*
 * A PasswordService that provides a default password policy
 * 
 * The intent of the password service is to encapsulate all password handling
 * details, such as password comparisons, hashing algorithm, hash iterations, salting policy, etc.
 * 
 * This class is just a wrapper around DefaultPasswordService to apply the default password policy,
 * and provide backward compatibility with legacy SHA1 and MD5 based passwords
 * 
 * @since 3.1
 */
@Singleton
@Typed( PasswordService.class )
@Named( "default" )
public class DefaultSecurityPasswordService
    implements HashingPasswordService
{   
    private static final String DEFAULT_HASH_ALGORITHM = "SHA-512";
    
    /**
     * Provides access to password hashing policy
     * Currently only provides hash iterations, but could be extended
     * at some point to include hashing algorithm, salt policy, etc
     */
    private final SecurityConfigurationManager securityConfiguration;
    
    /**
     * Provides the actual implementation of PasswordService.
     * We are just wrapping to apply default policy
     */
    private final DefaultPasswordService passwordService;
    
    /**
     * Provides password services for legacy passwords (e.g. pre-2.5 SHA-1/MD5-based hashes)
     */
    private final PasswordService legacyPasswordService;
    
    @Inject
    public DefaultSecurityPasswordService(SecurityConfigurationManager securityConfiguration,
                                       @Named("legacy") PasswordService legacyPasswordService)
    {
        this.securityConfiguration = securityConfiguration;
        this.passwordService = new DefaultPasswordService();
        this.legacyPasswordService = legacyPasswordService;

        //Create and set a hash service according to our hashing policies 
        DefaultHashService hashService = new DefaultHashService();
        hashService.setHashAlgorithmName(DEFAULT_HASH_ALGORITHM);
        hashService.setHashIterations(this.securityConfiguration.getHashIterations());
        hashService.setGeneratePublicSalt(true);
        this.passwordService.setHashService(hashService);
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
        //When hash is just a string, it could be a legacy password. Check both
        //current and legacy password services
        
        if(this.passwordService.passwordsMatch(submittedPlaintext, encrypted))
        {
            return true;
        }
        
        if(this.legacyPasswordService.passwordsMatch(submittedPlaintext,  encrypted))
        {
            return true;
        }
        
        return false;
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
