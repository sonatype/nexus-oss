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
package org.sonatype.nexus.configuration;

import java.util.Random;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ConfigurationIdGenerator.class )
public class DefaultConfigurationIdGenerator
    implements ConfigurationIdGenerator
{
    private Random rand = new Random( System.currentTimeMillis() );

    public String generateId()
    {
        return Long.toHexString( System.nanoTime() + rand.nextInt( 2008 ) );
    }

}
