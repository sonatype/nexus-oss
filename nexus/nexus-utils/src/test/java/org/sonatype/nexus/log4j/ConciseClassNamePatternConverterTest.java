/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.log4j;

import junit.framework.TestCase;

/**
 * @author juven
 */
public class ConciseClassNamePatternConverterTest
    extends TestCase
{
    public void testSimplify()
    {
        assertConciseClassName( "org.sonatype.Nexus", "org.sonatype.Nexus" );
        assertConciseClassName( "org.sonatype.nexus.T", "org.sonatype.nexus.T" );
        assertConciseClassName( "org.sonatype.nexus.Nexus", "o.s.n.Nexus" );
        assertConciseClassName( "org.sonatype.nexus.DefaultNexus", "o.s.n.DefaultNexus" );
        assertConciseClassName( "org.sonatype.nexus.MyDefaultWonderfulNexus", "o.s.n.MyDefaultWond~" );
    }

    protected void assertConciseClassName( String className, String conciseClassName )
    {
        assertEquals( conciseClassName, AbstractConcisePatternConverter.simplify( className, 20 ) );
    }
}
