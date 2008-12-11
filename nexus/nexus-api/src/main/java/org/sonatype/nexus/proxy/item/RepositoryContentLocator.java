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

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.StorageException;

/**
 * A content locator that gets file content based on it's UID, hence this locator is usable only with non-virtual file
 * items coming from a repository.
 * 
 * @author cstamas
 */
public class RepositoryContentLocator
    implements ContentLocator
{
    private RepositoryItemUid uid;

    public RepositoryContentLocator( RepositoryItemUid uid )
    {
        super();
        this.uid = uid;
    }

    public InputStream getContent()
        throws IOException
    {
        try
        {
            return uid.getRepository().retrieveItemContent( uid );
        }
        catch ( Exception ex )
        {
            throw new StorageException( "Cannot retrieve item content for " + uid.toString(), ex );
        }
    }

    public boolean isReusable()
    {
        return true;
    }
}
