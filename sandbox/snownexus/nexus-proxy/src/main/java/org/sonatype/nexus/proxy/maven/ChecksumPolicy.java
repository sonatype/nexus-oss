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
package org.sonatype.nexus.proxy.maven;

/**
 * Checksum policies known in Maven1/2 repositories where checksums are available according to maven layout.
 * 
 * @author cstamas
 */
public enum ChecksumPolicy
{
    /**
     * Will simply ignore remote checksums and Nexus will recalculate those.
     */
    IGNORE,

    /**
     * Will warn on bad checksums in logs but will serve what it has.
     */
    WARN,

    /**
     * In case of checksum inconsistency, Nexus will behave like STRICT, otherwise it will warn.
     */
    STRICT_IF_EXISTS,

    /**
     * In case of checksum inconsistency, Nexus will behave like the Artifact was not found -- will refuse to serve it.
     */
    STRICT;

    public boolean shouldCheckChecksum()
    {
        return !IGNORE.equals( this );
    }
}
