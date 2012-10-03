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

}
