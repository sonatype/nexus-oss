package org.sonatype.nexus.test.utils;

import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{
    public static XStream getXmlXStream()
    {
        XStream xs = new XStream( new LookAheadXppDriver() );

        NexusApplication napp = new NexusApplication();

        napp.doConfigureXstream( xs );

        return xs;
    }

    public static XStream getJsonXStream()
    {
        XStream xs = new XStream( new JsonOrgHierarchicalStreamDriver() );

        // for JSON, we use a custom converter for Maps
        xs.registerConverter( new PrimitiveKeyedMapConverter( xs.getMapper() ) );

        NexusApplication napp = new NexusApplication();

        napp.doConfigureXstream( xs );

        return xs;
    }

}
