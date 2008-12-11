/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.jsecurity;

import java.util.Random;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.tools.StringDigester;

@Component( role = PasswordGenerator.class )
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

    public String hashPassword( String password )
    {
        return StringDigester.getSha1Digest( password );
    }
}
