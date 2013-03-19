/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.usermanagement;

import java.security.SecureRandom;
import java.util.Random;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Hex;
import org.apache.shiro.crypto.hash.Sha512Hash;

/**
 * Default implementation of PasswordGenerator.
 */
@Singleton
@Typed( PasswordGenerator.class )
@Named( "default" )
public class DefaultPasswordGenerator
    implements PasswordGenerator
{
	private static final int SALT_LENGTH = 64;
	
    private int getRandom( int min, int max )
    {
        Random random = new Random();
        int total = max - min + 1;
        int next = Math.abs( random.nextInt() % total );

        return min + next;
    }

    @Override
    public String generatePassword( int minChars, int maxChars )
    {
        int length = getRandom( minChars, maxChars );

        byte bytes[] = new byte[length];

        for ( int i = 0; i < length; i++ )
        {
            if ( i % 2 == 0 )
            {
                bytes[i] = (byte) getRandom( 'a', 'z' );
            }
            else
            {
                bytes[i] = (byte) getRandom( '0', '9' );
            }
        }

        return new String( bytes );
    }
    
    @Override
	public String generateSalt()
    {
    	SecureRandom r = new SecureRandom();
    	byte[] salt = new byte[SALT_LENGTH];
    	r.nextBytes(salt);
    	return new String(Hex.encodeHex(salt));
	}

    @Override
    public String hashPassword( String password )
    {
        return StringDigester.getSha1Digest( password );
    }

	@Override
	public String hashPassword(String clearPassword,
							   String salt,
							   int hashIterations)
	{
		// set the password if its not null
        if ( clearPassword != null && clearPassword.trim().length() > 0 )
        {
        	return new Sha512Hash(clearPassword, salt, hashIterations).toHex();        	
        }

        return clearPassword;
	}
}
