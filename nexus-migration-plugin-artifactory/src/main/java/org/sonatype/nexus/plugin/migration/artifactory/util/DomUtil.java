/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
