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
package org.sonatype.nexus.proxy.maven.wl.internal;

/**
 * A matcher, that performs "path matching".
 * 
 * @author cstamas
 * @since 2.4
 */
public interface PathMatcher
{
    /**
     * Performs a match against passed in path.
     * 
     * @param path
     * @return {@code true} if path is matched, {@code false} otherwise.
     */
    boolean matches( String path );

    /**
     * Performs a match against passed in path, and returns {@code true} if it matches (same as {@link #matches(String)}
     * , or passed in path is a "parent" that is contained in one or more paths used for matching.
     * 
     * @param path
     * @return {@code true} if path is contained, {@code false} otherwise.
     */
    boolean contains( String path );
}
