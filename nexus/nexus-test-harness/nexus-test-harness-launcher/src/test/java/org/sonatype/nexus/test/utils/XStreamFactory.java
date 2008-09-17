package org.sonatype.nexus.test.utils;

import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;


import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{

    public static XStream getXmlXStream()
    {
        return XStreamInitializer.initialize( new XStream( new LookAheadXppDriver() ) );
    }

    public static XStream getJsonXStream()
    {
        XStream xstream = XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
        // for JSON, we use a custom converter for Maps
        xstream.registerConverter( new PrimitiveKeyedMapConverter( xstream.getMapper() ) );        
        return xstream;
    }

}
