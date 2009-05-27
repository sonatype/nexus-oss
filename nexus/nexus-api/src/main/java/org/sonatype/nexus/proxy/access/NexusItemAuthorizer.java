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
package org.sonatype.nexus.proxy.access;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Authorizes the Repository requests against permissions.
 * 
 * @author cstamas
 */
public interface NexusItemAuthorizer
{
    /**
     * Authorizes a TargetSet against an action. Used by authz filter to check the incoming request, that is obviously
     * addressed to content root.
     * 
     * @param repository
     * @param path
     * @return
     */
    boolean authorizePath( ResourceStoreRequest request, Action action );

    /**
     * Authorizes a repository level path against an action. Use when you have a repositoy path, ie. filtering of search
     * results or feeds.
     * 
     * @param repository
     * @param path
     * @return
     */
    boolean authorizePath( Repository repository, ResourceStoreRequest request, Action action );

    /**
     * A shorthand for "view" permission.
     * 
     * @param repository
     * @return
     */
    boolean isViewable( Repository repository );

    
    /**
     * Used to authorize a simple permission string
     * 
     * @param permission
     * @return
     */
    boolean authorizePermission( String permission );

}
