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
 * 
 * @author Juven Xu
 *
 */
public interface RepositoryConvertorFileHelper
{
    /**
     * move a file or folder to the target location, based on the basePath
     * 
     * @param file The file or folder to be moved
     * @param target The target repository
     * @param basePath The path based on which to run the moving.
     * @throws IOException
     */
    void move( File file, File target, String basePath )
        throws IOException;

    /**
     * copy a file or folder to the target location, based on the basePath
     * 
     * @param file The file or folder to be moved
     * @param target The target repository
     * @param basePath The path based on which to run the moving.
     * @throws IOException
     */
    void copy( File file, File target, String basePath )
        throws IOException;
}
