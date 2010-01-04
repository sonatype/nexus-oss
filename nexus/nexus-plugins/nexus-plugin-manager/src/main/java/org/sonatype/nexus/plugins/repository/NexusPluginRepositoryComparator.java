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
            return doCompare( o2, o1 );
        }
        else
        {
            return doCompare( o1, o2 );
        }
    }

    private int doCompare( NexusPluginRepository o1, NexusPluginRepository o2 )
    {
        int i = o1.getPriority() - o2.getPriority();
        if ( i != 0 )
        {
            return i;
        }
        return o1.getId().compareTo( o2.getId() );
    }
}
