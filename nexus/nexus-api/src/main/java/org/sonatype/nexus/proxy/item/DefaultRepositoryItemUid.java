/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The Class RepositoryItemUid. This class represents unique and constant label of all items/files originating from a
 * Repository, thus backed by some storage (eg. Filesystem).
 */
public class DefaultRepositoryItemUid
    implements RepositoryItemUid
{
    /** The repository. */
    private final Repository repository;

    /** The path. */
    private final String path;

    public DefaultRepositoryItemUid( Repository repository, String path )
    {
        super();

        this.repository = repository;

        this.path = path;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public String getPath()
    {
        return path;
    }

    /**
     * toString() will return a "string representation" of this UID in form of repoId + ":" + path
     */
    public String toString()
    {
        return getRepository().getId() + ":" + getPath();
    }

}
