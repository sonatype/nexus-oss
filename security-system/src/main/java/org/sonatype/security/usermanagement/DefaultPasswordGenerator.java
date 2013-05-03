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
package org.sonatype.security.usermanagement;

import java.util.Random;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Default implementation of PasswordGenerator.
 */
@Singleton
@Typed( PasswordGenerator.class )
@Named( "default" )
public class DefaultPasswordGenerator
    implements PasswordGenerator
{
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
    public String hashPassword( String password )
    {
        return StringDigester.getSha1Digest( password );
    }
}
