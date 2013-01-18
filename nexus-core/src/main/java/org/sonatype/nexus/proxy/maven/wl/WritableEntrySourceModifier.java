/*
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
package org.sonatype.nexus.proxy.maven.wl;

import java.io.IOException;

/**
 * A modifier for {@link WritableEntrySource}, that makes you able to "edit" it by adding and removing entries from it.
 * It performs entry source changes only when needed (entries added or removed does modify WL), and defers saving of
 * modified WL until you invoke {@link #apply()}.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface WritableEntrySourceModifier
{
    /**
     * Adds entries to {@link EntrySource} being modified. Returns {@code true} if the invocation actually did change
     * the WL. Changes are cached, entry source is not modified until you invoke {@link #apply()} method.
     * 
     * @param entries
     * @return {@code true} if the invocation actually did change the WL.
     */
    boolean offerEntries( String... entries );

    /**
     * Removes entries from {@link EntrySource} being modified. Returns {@code true} if the invocation actually did
     * change the WL. Changes are cached, entry source is not modified until you invoke {@link #apply()} method.
     * 
     * @param entries
     * @return {@code true} if the invocation actually did change the WL.
     */
    boolean revokeEntries( String... entries );

    /**
     * Returns {@code true} if this modifier has cached changes.
     * 
     * @return {@code true} if this modifier has cached changes.
     */
    boolean hasChanges();

    /**
     * Applies cached changes to backing {@link WritableEntrySource}. After returning from this method, backing
     * {@link WritableEntrySource} is modified (if there were cached changes). Returns {@code true} if there were cached
     * changes, and hence, the backing entry source did change.
     * 
     * @return {@code true} if there were cached changes and entry source was modified.
     * @throws IOException
     */
    boolean apply()
        throws IOException;

    /**
     * Resets this instance, by purging all the cached changes and reloads backing {@link WritableEntrySource} that
     * remained unchanged. Returns {@code true} if there were cached changes.
     * 
     * @return {@code true} if there were cached changes.
     * @throws IOException
     */
    boolean reset()
        throws IOException;
}
