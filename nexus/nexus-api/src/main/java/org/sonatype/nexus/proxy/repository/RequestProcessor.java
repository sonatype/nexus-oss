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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;

/**
 * A Processor that is able to process/modify the request before Nexus will serve it.
 * 
 * @author cstamas
 */
public interface RequestProcessor
{
    /**
     * A method that is able to modify the request _after_ it is authorized, but before it is executed. If the method
     * wants to completely stop the execution of this request, it should return false. Otherwise, true should be
     * returned.
     * 
     * @param request
     * @param action
     */
    boolean process( Repository repository, ResourceStoreRequest request, Action action );

    /**
     * Request processor is able to override generic behaviour of Repositories in aspect of proxying.
     * 
     * @param repository
     * @param uid
     * @param context
     * @return
     */
    boolean shouldProxy( ProxyRepository repository, ResourceStoreRequest request );

    /**
     * Request processor is able to override generic behaviour of Repository in aspect of caching.
     * 
     * @param repository
     * @param request
     * @param item
     * @return
     */
    boolean shouldCache( ProxyRepository repository, AbstractStorageItem item );
}
