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
