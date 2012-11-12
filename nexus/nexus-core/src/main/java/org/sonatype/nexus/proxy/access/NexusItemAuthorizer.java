/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.access;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * Authorizes the Repository requests against permissions.
 * 
 * @author cstamas
 */
public interface NexusItemAuthorizer
{
    public static final String VIEW_REPOSITORY_KEY = "repository";

    /**
     * Authorizes TargetSet.
     * @param matched
     * @param action
     * @return
     */
    public boolean authorizePath( TargetSet matched, Action action );

    /**
     * Returns groups for target set.
     * @param repository
     * @param request
     * @return
     */
    public TargetSet getGroupsTargetSet( Repository repository, ResourceStoreRequest request );

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
     * @param objectType
     * @param objectId
     * @return
     */
    boolean isViewable( String objectType, String objectId );

    
    /**
     * Used to authorize a simple permission string
     * 
     * @param permission
     * @return
     */
    boolean authorizePermission( String permission );

}
