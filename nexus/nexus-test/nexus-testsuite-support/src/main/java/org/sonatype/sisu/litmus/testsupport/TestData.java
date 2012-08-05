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
 * Test data access.
 *
 * @since 1.4
 */
public interface TestData
{

    /**
     * Resolves a test data file by looking up the specified path into data directory.
     * <p/>
     * It searches the following path locations:<br/>
     * {@code <dataDir>/<test class package>/<test class name>/<test method name>/</path>}<br/>
     * {@code <dataDir>/<test class package>/<test class name>/<path>}<br/>
     * {@code <dataDir>/<test class package>/<path>}<br/>
     * {@code <dataDir>/<path>}<br/>
     *
     * @param path path to look up
     * @return found file
     */
    File resolveFile( String path );

}
