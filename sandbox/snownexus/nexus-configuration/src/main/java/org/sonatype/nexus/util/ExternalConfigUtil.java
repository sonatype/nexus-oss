package org.sonatype.nexus.util;

import java.util.Collection;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ExternalConfigUtil
{
    public static void setNodeValue( Xpp3Dom parent, String name, String value )
    {
        // if we do not have a current value, then just return without setting the node;
        if ( value == null )
        {
            return;
        }

        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            node = new Xpp3Dom( name );

            parent.addChild( node );
        }

        node.setValue( value );
    }

    public static void setCollectionValues( Xpp3Dom parent, String nodeName, String childName,
        Collection<String> values )
    {
        Xpp3Dom node = parent.getChild( nodeName );

        if ( node != null )
        {
            for ( int i = 0; i < parent.getChildCount(); i++ )
            {
                Xpp3Dom existing = parent.getChild( i );

                if ( StringUtils.equals( nodeName, existing.getName() ) )
                {
                    parent.removeChild( i );

                    break;
                }
            }

            node = null;
        }

        node = new Xpp3Dom( nodeName );

        parent.addChild( node );

        for ( String childVal : values )
        {
            Xpp3Dom childNode = new Xpp3Dom( childName );
            node.addChild( childNode );
            childNode.setValue( childVal );
        }
    }
}
