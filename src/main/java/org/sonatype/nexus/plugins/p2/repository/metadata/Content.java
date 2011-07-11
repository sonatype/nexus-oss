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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Content
    extends AbstractMetadata
{
    public Content( final Xpp3Dom dom )
    {
        super( dom );
    }

    public Content( final String name )
    {
        super( new Xpp3Dom( "repository" ) );
        setRepositoryAttributes( name );
    }

    public void setRepositoryAttributes( final String name )
    {
        getDom().setAttribute( "name", name );
        getDom().setAttribute( "type", "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository" );
        getDom().setAttribute( "version", "1" );
    }

    public static class Unit
        extends AbstractMetadata
    {

        protected Unit( final Xpp3Dom dom )
        {
            super( dom );
        }

        public Unit( final Unit other )
        {
            super( other );
        }

        public String getId()
        {
            return dom.getAttribute( "id" );
        }

        public String getVersion()
        {
            return dom.getAttribute( "version" );
        }
    }

    public void removeReferences()
    {
        final Xpp3Dom[] children = dom.getChildren();

        for ( int i = 0; i < children.length; i++ )
        {
            if ( "references".equals( children[i].getName() ) )
            {
                dom.removeChild( i );
            }
        }
    }

    public List<Unit> getUnits()
    {
        final Xpp3Dom unitsDom = dom.getChild( "units" );

        return getUnits( unitsDom );
    }

    public static List<Unit> getUnits( final Xpp3Dom unitsDom )
    {
        final List<Unit> result = new ArrayList<Unit>();

        if ( unitsDom != null )
        {
            for ( final Xpp3Dom unitDom : unitsDom.getChildren( "unit" ) )
            {
                result.add( new Unit( unitDom ) );
            }
        }

        return result;
    }

    public void setUnits( final List<Unit> units )
    {
        removeChild( dom, "units" );
        final Xpp3Dom unitsDom = new Xpp3Dom( "units" );

        for ( final Unit unit : units )
        {
            unitsDom.addChild( unit.getDom() );
        }
        unitsDom.setAttribute( "size", Integer.toString( units.size() ) );

        dom.addChild( unitsDom );
    }

}
