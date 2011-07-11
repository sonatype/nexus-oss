/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.metadata;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractMetadata
{
    protected final Xpp3Dom dom;

    protected AbstractMetadata( final Xpp3Dom dom )
    {
        this.dom = dom;
    }

    protected AbstractMetadata( final AbstractMetadata other )
    {
        dom = new Xpp3Dom( other.dom );
    }

    public Xpp3Dom getDom()
    {
        return dom;
    }

    public static void removeChild( final Xpp3Dom dom, final String name )
    {
        Xpp3Dom[] children = dom.getChildren();
        for ( int i = 0; i < children.length; )
        {
            if ( name.equals( children[i].getName() ) )
            {
                dom.removeChild( i );
                children = dom.getChildren();
            }
            else
            {
                i++;
            }
        }
    }

    public void removeProperty( final String name )
    {
        final Xpp3Dom properties = dom.getChild( "properties" );

        if ( properties != null )
        {
            final Xpp3Dom[] property = properties.getChildren( "property" );

            for ( int i = 0; i < property.length; i++ )
            {
                if ( name.equals( property[i].getAttribute( "name" ) ) )
                {
                    properties.removeChild( i );
                }
            }
        }
    }

    public LinkedHashMap<String, String> getProperties()
    {
        final LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        final Xpp3Dom propertiesDom = dom.getChild( "properties" );

        if ( propertiesDom != null )
        {
            for ( final Xpp3Dom propertyDom : propertiesDom.getChildren( "property" ) )
            {
                result.put( propertyDom.getAttribute( "name" ), propertyDom.getAttribute( "value" ) );
            }
        }

        return result;
    }

    public void setProperties( final LinkedHashMap<String, String> properties )
    {
        removeChild( dom, "properties" );

        final Xpp3Dom propertiesDom = new Xpp3Dom( "properties" );

        for ( final Map.Entry<String, String> property : properties.entrySet() )
        {
            final Xpp3Dom propertyDom = new Xpp3Dom( "property" );
            propertyDom.setAttribute( "name", property.getKey() );
            propertyDom.setAttribute( "value", property.getValue() );
            propertiesDom.addChild( propertyDom );
        }

        propertiesDom.setAttribute( "size", Integer.toString( properties.size() ) );
        dom.addChild( propertiesDom );
    }

}
