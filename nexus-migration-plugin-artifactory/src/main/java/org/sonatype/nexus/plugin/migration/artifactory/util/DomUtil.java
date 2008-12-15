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
    
    public static Xpp3Dom findReference( Xpp3Dom dom )
    {
        String ref = dom.getAttribute( "reference" );

        Xpp3Dom currentDom = dom;

        String[] tokens = ref.split( "/" );

        for ( String token : tokens )
        {
            if ( token.equals( ".." ) )
            {
                currentDom = currentDom.getParent();
            }
            else if ( token.contains( "[" ) && token.contains( "]" ) )
            {
                int squareStart = token.indexOf( '[' );

                int squareEnd = token.indexOf( ']' );

                String childGroup = token.substring( 0, squareStart );

                String childIndex = token.substring( squareStart + 1, squareEnd );

                currentDom = currentDom.getChildren( childGroup )[Integer.parseInt( childIndex ) - 1];
            }
            else
            {
                currentDom = currentDom.getChild( token );
            }
        }
        return currentDom;
    }
}
