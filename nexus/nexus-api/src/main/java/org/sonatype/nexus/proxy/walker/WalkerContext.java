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
package org.sonatype.nexus.proxy.walker;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The WalkerContext is usable to control the walk and to share some contextual data during the wak.
 * 
 * @author cstamas
 */
public interface WalkerContext
{
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
     * Stops the walker.
     */
    void stop();

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
