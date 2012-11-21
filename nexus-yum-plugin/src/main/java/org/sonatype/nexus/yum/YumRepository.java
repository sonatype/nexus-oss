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
package org.sonatype.nexus.yum;

import java.io.File;

public interface YumRepository
{

    static final String PATH_OF_REPODATA = "repodata";

    static final String PATH_OF_FILELISTS_SQLLITE = PATH_OF_REPODATA + "/filelists.sqlite.bz2";

    static final String PATH_OF_FILELISTS_XML = PATH_OF_REPODATA + "/filelists.xml.gz";

    static final String PATH_OF_OTHER_SQLLITE = PATH_OF_REPODATA + "/other.sqlite.bz2";

    static final String PATH_OF_OTHER_XML = PATH_OF_REPODATA + "/other.xml.gz";

    static final String PATH_OF_PRIMARY_SQLLITE = PATH_OF_REPODATA + "/primary.sqlite.bz2";

    static final String PATH_OF_PRIMARY_XML = PATH_OF_REPODATA + "/primary.xml.gz";

    static final String PATH_OF_REPOMD_XML = PATH_OF_REPODATA + "/repomd.xml";

    static final String[] METADATA_FILES = {
        PATH_OF_FILELISTS_SQLLITE,
        PATH_OF_FILELISTS_XML,
        PATH_OF_OTHER_SQLLITE,
        PATH_OF_OTHER_XML,
        PATH_OF_PRIMARY_SQLLITE,
        PATH_OF_PRIMARY_XML,
        PATH_OF_REPOMD_XML
    };

    File getBaseDir();

    File getFile( String path );

    boolean isDirty();

    String getVersion();

    String getId();

}
