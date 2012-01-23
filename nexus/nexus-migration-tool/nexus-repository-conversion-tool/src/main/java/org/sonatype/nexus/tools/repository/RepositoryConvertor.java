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
 * Convert a repository mixed with release and snapshot versions to two, one with release versions and the other with
 * snapshot version.</br> For example, a repository named 'third-party' will be converted to 'third-party-releases' and
 * 'third-party-snapshots'.
 *
 * @author Juven Xu
 */
public interface RepositoryConvertor
{
    String SUFFIX_RELEASES = "-releases";

    String SUFFIX_SNAPSHOTS = "-snapshots";

    void convertRepositoryWithCopy( File repository, File targetPath )
        throws IOException;

    void convertRepositoryWithMove( File repository, File targetPath )
        throws IOException;

    void convertRepositoryWithCopy( File repository, File releasedTargetPath, File snapshotTargetPath, FileFilter filter )
        throws IOException;

    void convertRepositoryWithMove( File repository, File releasedTargetPath, File snapshotTargetPath, FileFilter filter )
        throws IOException;
}
