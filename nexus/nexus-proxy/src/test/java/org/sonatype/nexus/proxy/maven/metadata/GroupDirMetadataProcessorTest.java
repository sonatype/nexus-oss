package org.sonatype.nexus.proxy.maven.metadata;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.artifact.repository.metadata.Plugin;
import org.junit.Test;

public class GroupDirMetadataProcessorTest
{

    @Test
    public void testNoModelVersionForPluginGroupMetadata()
        throws IOException
    {
        DefaultMetadataHelper helper = new DefaultMetadataHelper( null, null )
        {

            @Override
            public void store( String content, String path )
                throws IOException
            {

                assertThat( content, not( containsString( "modelVersion" ) ) );
            }
            
        };
        Plugin plugin = new Plugin();
        plugin.setName( "pName" );
        plugin.setArtifactId( "aid" );
        plugin.setPrefix( "pPrefix" );
        helper.gData.put( "/gid", Arrays.asList( plugin ) );

        GroupDirMetadataProcessor processor = new GroupDirMetadataProcessor( helper );
        processor.processMetadata( "/gid" );
    }

}
