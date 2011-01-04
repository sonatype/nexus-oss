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
package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.uid.Attribute;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Repository item UID represents a key that uniquely identifies a resource in a repository. Every Item originating from
 * Nexus, that is not "virtual" is backed by UID with reference to it's originating Repository and path within that
 * repository. UIDs are immutable.
 * 
 * @author cstamas
 */
public interface RepositoryItemUid
{
    /** Constant to denote a separator in Proximity paths. */
    String PATH_SEPARATOR = "/";

    /** Constant to represent a root of the path. */
    String PATH_ROOT = PATH_SEPARATOR;

    /**
     * Gets the repository that is the origin of the item identified by this UID.
     * 
     * @return
     */
    Repository getRepository();

    /**
     * Gets the path that is the original path in the origin repository for resource with this UID.
     * 
     * @return
     */
    String getPath();

    /**
     * Locks this UID for action. Will perform lock upgrade is needed (read -> write). It is the responsibility of
     * caller to use lock/unlock properly (ie. boxing of calls).
     * 
     * @param action
     */
    void lock( Action action );

    /**
     * Unlocks UID. It is the responsibility of caller to use lock/unlock properly (ie. boxing of calls).
     * 
     * @param action
     */
    void unlock();

    /**
     * Locks attributes of item belonging to this UID for action. Will perform lock upgrade is needed (read -> write).
     * 
     * @param action
     */
    void lockAttributes( Action action );

    /**
     * Unlocks attributes of item belonging to this UID.
     */
    void unlockAttributes();

    /**
     * Gets an "attribute" from this UID.
     * 
     * @param <T>
     * @param attr
     * @return
     */
    <T extends Attribute<?>> T getAttribute( Class<T> attr );

    /**
     * Gets the value of the attribute from this UID, or null if no attribute found.
     * 
     * @param <T>
     * @param attr
     * @return
     */
    <A extends Attribute<V>, V> V getAttributeValue( Class<A> attr );

    /**
     * Gets the value of the attribute from this UID, or null if no attribute found.
     * 
     * @param <T>
     * @param attr
     * @return
     */
    <A extends Attribute<Boolean>> boolean getBooleanAttributeValue( Class<A> attr );
}
