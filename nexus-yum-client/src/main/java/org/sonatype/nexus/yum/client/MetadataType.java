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
package org.sonatype.nexus.yum.client;

import static org.sonatype.nexus.yum.client.internal.CompressionType.BZIP2;
import static org.sonatype.nexus.yum.client.internal.CompressionType.GZIP;
import static org.sonatype.nexus.yum.client.internal.CompressionType.NONE;

import org.sonatype.nexus.yum.client.internal.CompressionType;

public enum MetadataType
{
    REPOMD_XML( "/repodata/repomd.xml", NONE ), PRIMARY_XML( "/repodata/primary.xml.gz", GZIP ), PRIMARY_SQLITE(
    "/repodata/primary.sqlite.bz2", BZIP2 ), FILELIST_XML( "/repodata/filelist.xml.gz", GZIP ), FILELIST_SQLITE(
    "/repodata/filelist.sqlite.bz2", BZIP2 ), OTHER_XML( "/repodata/other.xml.gz", GZIP ), OTHER_SQLITE(
    "/repodata/other.sqlite.bz2", BZIP2 ), INDEX( "/", NONE );

    private final String path;

    private final CompressionType compression;

    private MetadataType( String path, CompressionType compression )
    {
        this.path = path;
        this.compression = compression;
    }

    public CompressionType getCompression()
    {
        return compression;
    }

    public String getPath()
    {
        return path;
    }

}
