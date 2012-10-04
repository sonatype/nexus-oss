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
package org.sonatype.nexus.plugins.yum.plugin.client.subsystem;

import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.CompressionType.BZIP2;
import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.CompressionType.GZIP;
import static org.sonatype.nexus.plugins.yum.plugin.client.subsystem.CompressionType.NONE;

public enum MetadataType
{
    REPOMD_XML( "repomd.xml", NONE ), PRIMARY_XML( "primary.xml.gz", GZIP ), PRIMARY_SQLITE( "primary.sqlite.bz2",
        BZIP2 ), FILELIST_XML( "filelist.xml.gz", GZIP ), FILELIST_SQLITE( "filelist.sqlite.bz2", BZIP2 ), OTHER_XML(
        "other.xml.gz", GZIP ), OTHER_SQLITE( "other.sqlite.bz2", BZIP2 );

    private final String filename;

    private final CompressionType compression;

    private MetadataType( String filename, CompressionType compression )
    {
        this.filename = filename;
        this.compression = compression;
    }

    public String getFilename()
    {
        return filename;
    }

    public CompressionType getCompression()
    {
        return compression;
    }

    public String getPath()
    {
        return "/repodata/" + filename;
    }

}
