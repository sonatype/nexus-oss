package org.sonatype.nexus.test.utils.plugin;

import org.sonatype.nexus.test.utils.XStreamConfigurator;

import com.thoughtworks.xstream.XStream;

/**
 * Plugin XStream factory, meant for Plugin ITs, that applies Nexus Core configuration, but also add preferred (usually
 * plugin-specific) configuration too.
 * 
 * @author cstamas
 */
public class XStreamFactory
{
    public static XStream getXmlXStream( XStreamConfigurator configurator )
    {
        XStream xs = org.sonatype.nexus.test.utils.XStreamFactory.getXmlXStream();

        configureXStream( xs, configurator );

        return xs;
    }

    public static XStream getJsonXStream( XStreamConfigurator configurator )
    {
        XStream xs = org.sonatype.nexus.test.utils.XStreamFactory.getJsonXStream();

        configureXStream( xs, configurator );

        return xs;
    }

    private static void configureXStream( XStream xstream, XStreamConfigurator configurator )
    {
        if ( configurator != null )
        {
            configurator.configure( xstream );
        }
    }
}
