/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.appcontext;

import java.util.Map;

import org.sonatype.appcontext.lifecycle.AppContextLifecycleManager;

/**
 * The generic app context, which is actually a Map. For modification, you can use only the {@link #put(String, Object)}
 * method, {@link #remove(Object)} method, {@link #putAll(Map)} and {@link #clear()} methods, since all the "collection"
 * methods like {@link #keySet()} {@link #values()} and {@link #entrySet()} returns unmodifiable "views" only!
 * 
 * @author cstamas
 */
public interface AppContext
    extends Map<String, Object>
{
    /**
     * A key to be used for mapping whenever needed, to find AppContext. This key is merely to be used in Map-like
     * mappings.
     */
    String APPCONTEXT_KEY = AppContext.class.getName();

    /**
     * Returns the id of this context.
     * 
     * @return the ID of this context.
     */
    String getId();

    /**
     * Returns the parent app context if any, or {@code null} if this context is root context.
     * 
     * @return the parent app context.
     */
    AppContext getParent();

    /**
     * Returns the time stamp in milliseconds when this context was created.
     * 
     * @return the creation time in milliseconds.
     */
    long getCreated();

    /**
     * Returns the time stamp in milliseconds when this context was last modified.
     * 
     * @return the creation time in milliseconds.
     */
    long getModified();

    /**
     * Returns the "generation" of the app context, usable for change detection. It is guaranteed, that when a change
     * happens against this context ({@link #put(String, Object)}, {@link #putAll(Map)}, {@link #remove(Object)} methods
     * are invoked), the integer returned by this method will be different than it was returned before the invocations
     * of changing methods. This method is better suited for change detection, as the {@link #getModified()} might
     * "oversee" changes happening in same millisecond.
     * 
     * @return an integer marking the "generation" of this instance. On changes made to this context is grows. It's
     *         stepping is undefined, so only "equality" or relations like "bigger" or "smaller" might be checked
     *         against it.
     * @since 3.2
     */
    int getGeneration();

    /**
     * Returns the context's lifecycle manager.
     * 
     * @return the lifecycle manager of this context.
     * @since 3.1
     */
    AppContextLifecycleManager getLifecycleManager();

    /**
     * Flattens this AppContext (calculates "visible" entries from this and it's parent and returns a plain Map. This
     * map is not connected to AppContext anymore, and not modifiable! It is just a "snapshot".
     * 
     * @return a "snapshot" of entries of this app context hierarchy as unmodifiable map.
     */
    Map<String, Object> flatten();

    /**
     * Interpolates passed in string using this app context as source.
     * 
     * @param input the input string to interpolate. Keys are marked as ${key}.
     * @return interpolated string using this app context.
     * @throws AppContextInterpolationException in case of interpolation problem (like recursive evaluation etc).
     * @since 3.0
     */
    String interpolate( String input )
        throws AppContextInterpolationException;

    /**
     * Returns the entry value, used in creation of this context. Gives access to source marker and raw (uninterpolated)
     * values. Low level method!
     * 
     * @param key the key of the entry you want.
     * @return the {@link AppContextEntry} corresponding to given {@code key} or {@code null} if no such entry.
     */
    AppContextEntry getAppContextEntry( String key );

    /**
     * Flattens this AppContext (calculates "visible" entries from this and it's parent and returns a plain Map but with
     * AppContextEntries as values. This map is not connected to AppContext anymore, and not modifiable! It is just a
     * "snapshot". Low level method! Similar to {@link #flatten()} method, but here, you get the {@link AppContextEntry}
     * instances.
     * 
     * @return a "snapshot" of {@link AppContextEntry}s of this app context hierarchy as unmodifiable map.
     */
    Map<String, AppContextEntry> flattenAppContextEntries();
}
