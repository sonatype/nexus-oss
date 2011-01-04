/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component doing "cleanup" (after a repository removal) of anything needed, for example removing directories or so.
 * 
 * @author cstamas
 */
public interface RepositoryFolderCleaner
{
    /**
     * Performs the needed cleanup after repository already removed from system, that had ID as passed in.
     * 
     * @param repository the repository removed (WARNING: RepositoryRegistry does not contains it anymore!).
     * @param deleteForever true if removal wanted, false if just "move to trash".
     * @throws IOException
     */
    void cleanRepositoryFolders( Repository repository, boolean deleteForever )
        throws IOException;
}
