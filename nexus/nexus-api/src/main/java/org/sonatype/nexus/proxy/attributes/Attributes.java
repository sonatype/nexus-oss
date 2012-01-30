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
package org.sonatype.nexus.proxy.attributes;

import java.util.Map;

/**
 * Attributes are simply a String key-value pairs with some type-safe getters and setters for keys known and used in
 * core.
 * 
 * @author cstamas
 * @since 2.0
 */
public interface Attributes
{
    /**
     * Returns true if this instance has a value for given key set.
     * 
     * @param key
     * @return
     */
    boolean containsKey( final String key );

    /**
     * Gets the value for given key.
     * 
     * @param key
     * @return
     */
    String get( final String key );

    /**
     * Puts the value for given key, returning any previous values bound. Note: Attributes does not allow {@code null}
     * for key neither values!
     * 
     * @param key
     * @param value
     * @return
     */
    String put( final String key, final String value );

    /**
     * Removes the value with given key from this instance.
     * 
     * @param key
     * @return
     */
    String remove( final String key );

    /**
     * Puts all the entries of the given map into this instance.
     * 
     * @param map
     */
    void putAll( Map<? extends String, ? extends String> map );

    // ===

    /**
     * Performs an "overlay" with given attributes: only those values are changed that are not set by some setter method
     * or by {@link #put(String, String)} or by {@link #putAll(Map)}. Core internal use only!
     * 
     * @param repositoryItemAttributes
     */
    void overlayAttributes( final Attributes repositoryItemAttributes );

    /**
     * Returns the generation of Attributes. Core internal use only!
     * 
     * @return
     */
    int getGeneration();

    /**
     * Sets the generation of Attributes. Core internal use only!
     * 
     * @param value
     */
    void setGeneration( final int value );

    /**
     * Setps the generation of this instance. Core internal use only!
     */
    void incrementGeneration();

    /**
     * Returns the path attribute.
     * 
     * @return
     */
    String getPath();

    /**
     * Sets the path attribute.
     * 
     * @param value
     */
    void setPath( final String value );

    /**
     * Returns the readable attribute.
     * 
     * @return
     */
    boolean isReadable();

    /**
     * Sets the readable attribute.
     * 
     * @param value
     */
    void setReadable( final boolean value );

    /**
     * Returns the writable attribute.
     * 
     * @return
     */
    boolean isWritable();

    /**
     * Sets the writable attribute.
     * 
     * @param value
     */
    void setWritable( final boolean value );

    /**
     * Returns the repositoryId attribute.
     * 
     * @return
     */
    String getRepositoryId();

    /**
     * Sets the repositoryId attribute.
     * 
     * @param value
     */
    void setRepositoryId( final String value );

    /**
     * Returns the created attribute.
     * 
     * @return
     */
    long getCreated();

    /**
     * Sets the created attribute.
     * 
     * @param value
     */
    void setCreated( final long value );

    /**
     * Returns the modified attribute.
     * 
     * @return
     */
    long getModified();

    /**
     * Sets the modified attribute.
     * 
     * @param value
     */
    void setModified( final long value );

    /**
     * Returns the storedLocally attribute.
     * 
     * @return
     */
    long getStoredLocally();

    /**
     * Sets the storedLocally attribute.
     * 
     * @param value
     */
    void setStoredLocally( final long value );

    /**
     * Returns the checkedRemotely attribute.
     * 
     * @return
     */
    long getCheckedRemotely();

    /**
     * Sets the checkedRemotely attribute.
     * 
     * @param value
     */
    void setCheckedRemotely( final long value );

    /**
     * Returns the lastRequest attribute.
     * 
     * @return
     */
    long getLastRequested();

    /**
     * Sets the lastRequested attribute.
     * 
     * @param value
     */
    void setLastRequested( final long value );

    /**
     * Returns the expired attribute.
     * 
     * @return
     */
    boolean isExpired();

    /**
     * Sets the expired attribute.
     * 
     * @param value
     */
    void setExpired( final boolean value );

    /**
     * Returns the remoteUrl attribute.
     * 
     * @return
     */
    String getRemoteUrl();

    /**
     * Sets the remoteUrl attribute.
     * 
     * @param value
     */
    void setRemoteUrl( final String value );

    /**
     * Returns the length attribute.
     * 
     * @return
     */
    long getLength();

    /**
     * Sets the length attribute.
     * 
     * @param value
     */
    void setLength( final long value );

    // ==

    /**
     * Returns an unmodifiable "snapshot" of this instance as {@code Map<String, String>}.
     * 
     * @return
     */
    Map<String, String> asMap();
}
