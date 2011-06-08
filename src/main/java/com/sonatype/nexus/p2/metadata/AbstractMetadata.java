/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.metadata;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractMetadata
{
    protected final Xpp3Dom dom;

    protected AbstractMetadata( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    protected AbstractMetadata( AbstractMetadata other )
    {
        this.dom = new Xpp3Dom( other.dom );
    }

    public Xpp3Dom getDom()
    {
        return dom;
    }

    public static void removeChild( Xpp3Dom dom, String name )
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

    public void removeProperty( String name )
    {
        Xpp3Dom properties = dom.getChild( "properties" );

        if ( properties != null )
        {
            Xpp3Dom[] property = properties.getChildren( "property" );

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
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        
        Xpp3Dom propertiesDom = dom.getChild( "properties" );
        
        if ( propertiesDom != null )
        {
            for ( Xpp3Dom propertyDom : propertiesDom.getChildren( "property" ) )
            {
                result.put( propertyDom.getAttribute( "name" ), propertyDom.getAttribute( "value" ) );
            }
        }

        return result;
    }

    public void setProperties( LinkedHashMap<String, String> properties )
    {
        removeChild( dom, "properties" );
        
        Xpp3Dom propertiesDom = new Xpp3Dom( "properties" );
        
        for ( Map.Entry<String, String> property : properties.entrySet() )
        {
            Xpp3Dom propertyDom = new Xpp3Dom( "property" );
            propertyDom.setAttribute( "name", property.getKey() );
            propertyDom.setAttribute( "value", property.getValue() );
            propertiesDom.addChild( propertyDom );
        }

        propertiesDom.setAttribute( "size", Integer.toString( properties.size() ) );
        dom.addChild( propertiesDom );
    }

}
