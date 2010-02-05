package org.sonatype.nexus.index.events;

@Deprecated
public class ReindexRepositoriesRequest
{
    private String path;

    private boolean fullReindex;

    public ReindexRepositoriesRequest( String path, boolean fullReindex )
    {
        this.path = path;
        this.fullReindex = fullReindex;
    }

    public String getPath()
    {
        return path;
    }

    public boolean isFullReindex()
    {
        return fullReindex;
    }    
}
