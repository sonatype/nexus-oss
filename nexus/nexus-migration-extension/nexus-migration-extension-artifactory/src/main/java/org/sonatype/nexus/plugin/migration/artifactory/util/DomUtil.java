package org.sonatype.nexus.plugin.migration.artifactory.util;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DomUtil
{

    public static String getValue( Xpp3Dom dom, String nodeName )
    {
        Xpp3Dom node = dom.getChild( nodeName );
        if ( node == null )
        {
            return null;
        }
        return node.getValue();
    }

}
