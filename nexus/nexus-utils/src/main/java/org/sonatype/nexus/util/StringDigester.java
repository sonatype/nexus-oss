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
package org.sonatype.nexus.util;

/**
 * A util class to calculate various digests on Strings. Usaful for some simple password management.
 * 
 * @author cstamas
 * @deprecated Use DigesterUtils instead!
 */
public class StringDigester
{
    /**
     * Calculates a SHA1 digest for a string.
     * 
     * @param content
     * @return
     */
    public static String getSha1Digest( String content )
    {
        return DigesterUtils.getSha1Digest( content );
    }

    /**
     * Calculates MD5 digest for a string.
     * 
     * @param content
     * @return
     */
    public static String getMd5Digest( String content )
    {
        return DigesterUtils.getMd5Digest( content );
    }
}
