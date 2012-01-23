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
package org.sonatype.sample.wrapper;

import java.util.Map;

/**
 * The "low level" abstraction around wrapper.conf, or any other Java properties-like file that uses similar notation
 * for lists like wrapper.conf does (".1 .2 etc" suffixes to keys).
 * 
 * @author cstamas
 */
public interface WrapperConfWrapper
    extends PersistedConfiguration
{
    /**
     * Getter for "single-keyed" properties. Will not look for ".1 .2, etc" suffixes.
     * 
     * @param key
     * @param defaultValue
     * @return
     */
    String getProperty( String key, String defaultValue );

    /**
     * Getter for "single-keyed" properties. Will not look for ".1 .2, etc" suffixes. As default value, returns null.
     * 
     * @param key
     * @param defaultValue
     * @return the found value, or null if key not found.
     */
    String getProperty( String key );

    /**
     * Getter for "single-keyed" properties as integer. Will not look for ".1 .2, etc" suffixes.
     * 
     * @param key
     * @param defaultValue
     * @return the found value, or null if key not found.
     */
    int getIntegerProperty( String key, int defaultValue );

    /**
     * Setter for "single-keyed" properties. Will not try to figure out ".1 .2 etc suffixes.
     * 
     * @param key
     * @param value
     */
    void setProperty( String key, String value );

    /**
     * Setter for "single-keyed" properties as integer. Will not try to figure out ".1 .2 etc suffixes.
     * 
     * @param key
     * @param value
     */
    void setIntegerProperty( String key, int value );

    /**
     * Setter for "single-keyed" properties. Will not try to figure out ".1 .2 etc suffixes. Removes the key=value pair
     * from file.
     * 
     * @param key
     * @return true if founf and removed
     */
    boolean removeProperty( String key );

    /**
     * Getter for "multi-keyed" properties. Here, the key is appended by ".1 .2 etc" suffixes to read the values, as
     * needed.
     * 
     * @param key
     * @return
     */
    String[] getPropertyList( String key );

    /**
     * Setter for "multi-keyed" properties. Here, the key will be appended with ".1 .2 etc" suffixes when written, as
     * needed in order as in supplied array. Supplying null as values, or empty array WILL DELETE the settings.
     * 
     * @param key
     * @param values the values to write to. If null or empty array, it will result in DELETION of params.
     */
    void setPropertyList( String key, String[] values );

    /**
     * Returns a Map of all properties found in this file, of form "key = value". This method may be used to enumerate
     * all the existing keys or values in file.
     * 
     * @return
     */
    Map<String, String> getAllKeyValuePairs();
}
