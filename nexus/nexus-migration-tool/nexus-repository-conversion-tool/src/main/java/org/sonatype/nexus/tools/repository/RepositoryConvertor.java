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
package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.IOException;

/**
 * Convert a repository mixed with release and snapshot versions to two, one with release versions and the other with
 * snapshot version.</br> For example, a repository named 'third-party' will be converted to 'third-party-releases' and
 * 'third-party-snapshots'.
 * 
 * @author Juven Xu
 */
public interface RepositoryConvertor
{
    String SUFFIX_RELEASES = "-releases";

    String SUFFIX_SNAPSHOTS = "-snapshots";

    void convertRepositoryWithCopy( File repository, File targetPath )
        throws IOException;

    void convertRepositoryWithMove( File repository, File targetPath )
        throws IOException;
}
