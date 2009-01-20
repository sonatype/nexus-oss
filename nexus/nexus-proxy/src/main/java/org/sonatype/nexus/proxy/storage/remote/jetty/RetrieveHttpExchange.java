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
package org.sonatype.nexus.proxy.storage.remote.jetty;

import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.HttpStatus;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;

public class RetrieveHttpExchange
    extends AbstractNexusExchange
{
    public RetrieveHttpExchange( ProxyRepository repository, String path )
    {
        super( repository, path );

        setMethod( HttpMethods.GET );
    }

    @Override
    protected boolean doValidate()
        throws ItemNotFoundException,
            StorageException
    {
        if ( getResponseStatus() == HttpStatus.ORDINAL_200_OK )
        {
            // ok
            return true;
        }
        else
        {
            // unexpected
            return false;
        }
    }

    public AbstractStorageItem getStorageItem()
    {
        return null;
    }

}
