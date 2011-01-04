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
package org.sonatype.nexus.proxy.storage;

/**
 * The storage settings and context.
 * 
 * @author cstamas
 */
public interface StorageContext
{
    // change detection

    /**
     * Returns the timestamp of latest change. Will propagate to parent if it is more recently changed as this (and
     * parent is set).
     */
    long getLastChanged();

    // parent

    /**
     * Returns the parent context, or null if not set.
     * 
     * @return
     */
    StorageContext getParentStorageContext();

    /**
     * Sets the parent context, or nullify it.
     * 
     * @return
     */
    void setParentStorageContext( StorageContext parent );

    // modification

    /**
     * Gets an object from context. Will propagate to parent if not found in this context (and parent is set).
     * 
     * @param key
     * @return
     */
    Object getContextObject( String key );

    /**
     * Puts an object into this context.
     * 
     * @param key
     * @param value
     */
    void putContextObject( String key, Object value );

    /**
     * Removed an object from this context. Parent is unchanged.
     * 
     * @param key
     */
    void removeContextObject( String key );

    /**
     * Returns true if this context has an object under the given key.
     * 
     * @param key
     * @return
     */
    boolean hasContextObject( String key );
}
