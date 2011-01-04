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
package org.sonatype.nexus.proxy.walker;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The WalkerContext is usable to control the walk and to share some contextual data during the wak.
 * 
 * @author cstamas
 */
public interface WalkerContext
{
    /**
     * Gets the resource store request that initiated this walk.
     * 
     * @return
     */
    ResourceStoreRequest getResourceStoreRequest();

    /**
     * Will not try to reach remote storage.
     * 
     * @return
     */
    boolean isLocalOnly();

    /**
     * The WalkerProcessors will get notified only on collections.
     * 
     * @return
     */
    boolean isCollectionsOnly();

    /**
     * Returns the context.
     * 
     * @return
     */
    Map<String, Object> getContext();

    /**
     * Gets (and creates in null and empty list) the list of processors.
     * 
     * @return
     */
    List<WalkerProcessor> getProcessors();

    /**
     * Sets the list of processors to use.
     * 
     * @param processors
     */
    void setProcessors( List<WalkerProcessor> processors );

    /**
     * Stops the walker with cause.
     * 
     * @param cause
     */
    void stop( Throwable cause );

    /**
     * Returns true is walker is stopped in the middle of walking.
     * 
     * @return
     */
    boolean isStopped();

    /**
     * Returns the cause of stopping this walker or null if none is given.
     * 
     * @return
     */
    Throwable getStopCause();

    /**
     * Returns the filter used in walk or null.
     * 
     * @return the used filter or null.
     */
    WalkerFilter getFilter();

    /**
     * Returns the resource store instance that is/will be walked over.
     * 
     * @return
     */
    Repository getRepository();
}
