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
package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * @author Juven Xu
 */
public interface RepositoryConvertorFileHelper
{
    /**
     * move a file or folder to the target location, based on the basePath
     *
     * @param file The file or folder to be moved
     * @param target The target repository
     * @param basePath The path based on which to run the moving.
     * @throws IOException
     */
    void move( File file, File target, String basePath )
        throws IOException;

    void move( File file, File target, String basePath, FileFilter filter )
        throws IOException;

    /**
     * copy a file or folder to the target location, based on the basePath
     *
     * @param file The file or folder to be moved
     * @param target The target repository
     * @param basePath The path based on which to run the moving.
     * @throws IOException
     */
    void copy( File file, File target, String basePath )
        throws IOException;

    void copy( File file, File target, String basePath, FileFilter filter )
        throws IOException;
}
