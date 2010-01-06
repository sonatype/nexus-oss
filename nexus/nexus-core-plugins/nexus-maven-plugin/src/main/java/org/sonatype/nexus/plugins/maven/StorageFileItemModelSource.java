package org.sonatype.nexus.plugins.maven;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.building.ModelSource;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class StorageFileItemModelSource
    implements ModelSource
{
    private final StorageFileItem pomFileItem;

    public StorageFileItemModelSource( StorageFileItem pomFileItem )
    {
        this.pomFileItem = pomFileItem;
    }

    public StorageFileItem getPomFileItem()
    {
        return pomFileItem;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return pomFileItem.getInputStream();
    }

    public String getLocation()
    {
        return pomFileItem.getRepositoryItemUid().toString();
    }
}
