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
package org.sonatype.sisu.litmus.testsupport;

import java.io.File;

/**
 * Test related directory index.
 *
 * @since 1.4
 */
public interface TestIndex
{

    /**
     * Returns a test specific directory of format ${indexDir}/${counter}.
     * If directory does not exist yet, it will be created.
     *
     * @return test specific directory
     */
    File getDirectory();

    /**
     * Returns a test specific directory of format ${indexDir}/${counter}/${name}.
     * If directory does not exist yet, it will be created.
     *
     * @param name name of test specific directory
     * @return test specific directory. Never null
     */
    File getDirectory( String name );

    /**
     * Records information about current running test.
     *
     * @param key   information key
     * @param value information value
     */
    void recordInfo( String key, String value );

    /**
     * Records information about current running test.
     * The value is considered to be a link.
     *
     * @param key   information key
     * @param value information value
     */
    void recordLink( String key, String value );

    /**
     * Records information about current running test.
     * The value is considered to be a link to a file.
     *
     * @param key  information key
     * @param file information value
     */
    void recordLink( String key, File file );

}