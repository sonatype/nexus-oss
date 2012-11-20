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
package org.sonatype.nexus.repository.yum;

import java.io.File;

public interface YumRepository
{

    public static final String PATH_OF_FILELISTS_SQLLITE = "repodata/filelists.sqlite.bz2";

    public static final String PATH_OF_FILELISTS_XML = "repodata/filelists.xml.gz";

    public static final String PATH_OF_OTHER_SQLLITE = "repodata/other.sqlite.bz2";

    public static final String PATH_OF_OTHER_XML = "repodata/other.xml.gz";

    public static final String PATH_OF_PRIMARY_SQLLITE = "repodata/primary.sqlite.bz2";

    public static final String PATH_OF_PRIMARY_XML = "repodata/primary.xml.gz";

    public static final String PATH_OF_REPOMD_XML = "repodata/repomd.xml";

    public static final String[] METADATA_FILES = {
        PATH_OF_FILELISTS_SQLLITE,
        PATH_OF_FILELISTS_XML,
        PATH_OF_OTHER_SQLLITE,
        PATH_OF_OTHER_XML,
        PATH_OF_PRIMARY_SQLLITE,
        PATH_OF_PRIMARY_XML,
        PATH_OF_REPOMD_XML
    };

    public File getBaseDir();

    public File getFile( String path );

    public boolean isDirty();

    public String getVersion();

    public String getId();

}
