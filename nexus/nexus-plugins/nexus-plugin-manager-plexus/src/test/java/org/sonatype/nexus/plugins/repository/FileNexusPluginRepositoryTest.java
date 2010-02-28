package org.sonatype.nexus.plugins.repository;

import org.sonatype.nexus.plugins.AbstractNexusPluginManagerTest;
import org.sonatype.plugin.metadata.GAVCoordinate;

public class FileNexusPluginRepositoryTest
    extends AbstractNexusPluginManagerTest
{
    private NexusPluginRepository nexusPluginRepository;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusPluginRepository = lookup( NexusPluginRepository.class, UserNexusPluginRepository.ID );
    }

    public void testSimple()
        throws Exception
    {
        AbstractFileNexusPluginRepository trick = (AbstractFileNexusPluginRepository) nexusPluginRepository;

        GAVCoordinate gav = null;

        String fileName = null;

        gav = new GAVCoordinate( "g:a:v" );

        fileName = trick.getPluginFileName( gav );

        assertEquals( "a-v.jar", fileName );

        gav = new GAVCoordinate( "g:a:v:c" );

        fileName = trick.getPluginFileName( gav );

        assertEquals( "a-v-c.jar", fileName );

        gav = new GAVCoordinate( "g:a:v:c:nexus-plugin" );

        fileName = trick.getPluginFileName( gav );

        assertEquals( "a-v-c.jar", fileName );

        gav = new GAVCoordinate( "g:a:v::nexus-plugin" );

        fileName = trick.getPluginFileName( gav );

        assertEquals( "a-v.jar", fileName );
    }

}
