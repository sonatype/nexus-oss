package org.sonatype.nexus.index;


public class DisableIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    public void testDisableIndex()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexRepository( "/", snapshots.getId(), false );

        searchFor( "org.sonatype.plexus", 1 );

        snapshots.setIndexable( false );

        nexusConfiguration.saveConfiguration();

        searchFor( "org.sonatype.plexus", 0 );
    }
}
