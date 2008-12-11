/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.configuration.model.CRepository;

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

    public static ChecksumPolicy fromModel( String string )
    {
        if ( CRepository.CHECKSUM_POLICY_IGNORE.equals( string ) )
        {
            return IGNORE;
        }
        else if ( CRepository.CHECKSUM_POLICY_WARN.equals( string ) )
        {
            return WARN;
        }
        else if ( CRepository.CHECKSUM_POLICY_STRICT.equals( string ) )
        {
            return STRICT;
        }
        else if ( CRepository.CHECKSUM_POLICY_STRICT_IF_EXISTS.equals( string ) )
        {
            return STRICT_IF_EXISTS;
        }
        else
        {
            return null;
        }
    }

    public static String toModel( ChecksumPolicy policy )
    {
        return policy.toString();
    }

    public String toString()
    {
        if ( IGNORE.equals( this ) )
        {
            return CRepository.CHECKSUM_POLICY_IGNORE;
        }
        else if ( WARN.equals( this ) )
        {
            return CRepository.CHECKSUM_POLICY_WARN;
        }
        else if ( STRICT.equals( this ) )
        {
            return CRepository.CHECKSUM_POLICY_STRICT;
        }
        else if ( STRICT_IF_EXISTS.equals( this ) )
        {
            return CRepository.CHECKSUM_POLICY_STRICT_IF_EXISTS;
        }
        else
        {
            return null;
        }
    }
}
