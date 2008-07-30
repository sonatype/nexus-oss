/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security;

import java.util.Random;

/**
 * @plexus.component
 */
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
    
    public String generatePassword( int minChars, int maxChars)
    {
        int length = getRandom( minChars, maxChars );
        
        byte bytes[] = new byte[length];
        
        for ( int i = 0 ; i < length ; i++ )
        {
            if ( i % 2 == 0 )
            {
                bytes[i] = ( byte )getRandom( 'a', 'z' );
            }
            else
            {
                bytes[i] = ( byte )getRandom( '0', '9' );
            }
        }
        
        return new String(bytes);
    }

    public String hashPassword( String password )
    {
        // TODO Auto-generated method stub
        return null;
    }

}
