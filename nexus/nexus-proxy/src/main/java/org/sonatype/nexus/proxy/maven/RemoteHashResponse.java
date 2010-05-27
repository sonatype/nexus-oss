package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;

public class RemoteHashResponse
{

    private DefaultStorageFileItem hashItem;

    private String inspector;

    private String remoteHash;

    public RemoteHashResponse( String inspector, String remoteHash, DefaultStorageFileItem hashItem )
    {
        super();
        this.inspector = inspector;
        this.remoteHash = remoteHash;
        this.hashItem = hashItem;
    }

    public DefaultStorageFileItem getHashItem()
    {
        return hashItem;
    }

    public String getInspector()
    {
        return inspector;
    }

    public String getRemoteHash()
    {
        return remoteHash;
    }

}
