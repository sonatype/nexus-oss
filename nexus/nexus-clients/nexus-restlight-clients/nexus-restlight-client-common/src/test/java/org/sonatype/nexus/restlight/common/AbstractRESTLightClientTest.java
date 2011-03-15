package org.sonatype.nexus.restlight.common;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

public class AbstractRESTLightClientTest
{

    @Test
    public void testGetVocabilary()
        throws Exception
    {
        AbstractRESTLightClient c = new AbstractRESTLightClient( null, null, null, null )
        {
            @Override
            protected void connect()
                throws RESTLightClientException
            {
                // do nothing
            }
        };

        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        map.put( "1.0", Arrays.asList( "a", "b" ) );
        map.put( "1.9", Arrays.asList( "a", "b", "c", "d" ) );

        List<String> voc = c.getVocabilary( map, "1.0" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b" ) );

        voc = c.getVocabilary( map, "1.1" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b" ) );

        voc = c.getVocabilary( map, "1.9" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b", "c", "d" ) );

        voc = c.getVocabilary( map, "1.10" );
        assertThat( voc, IsCollectionContaining.hasItems( "a", "b", "c", "d" ) );
    }

}
