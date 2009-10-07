package org.sonatype.nexus.plugins.repository;

import java.util.Comparator;

public class NexusPluginRepositoryComparator
    implements Comparator<NexusPluginRepository>
{
    private final boolean reverse;

    public NexusPluginRepositoryComparator()
    {
        this( false );
    }

    public NexusPluginRepositoryComparator( boolean reverse )
    {
        this.reverse = reverse;
    }

    public int compare( NexusPluginRepository o1, NexusPluginRepository o2 )
    {
        if ( reverse )
        {
            return o2.getPriority() - o1.getPriority();
        }
        else
        {
            return o1.getPriority() - o2.getPriority();
        }
    }
}
