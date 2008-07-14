/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.maven;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * A very simple artifact packaging mapper, that has everyting wired in this class.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultArtifactPackagingMapper
    extends AbstractLogEnabled
    implements ArtifactPackagingMapper
{
    // TODO: think about externalizing this map to a file, and make it user extendable

    private final Map<String, String> typeToExtensions;

    {
        typeToExtensions = new HashMap<String, String>();
        typeToExtensions.put( "ejb-client", "jar" );
        typeToExtensions.put( "ejb", "jar" );
        typeToExtensions.put( "rar", "jar" );
        typeToExtensions.put( "par", "jar" );
        typeToExtensions.put( "maven-plugin", "jar" );
        typeToExtensions.put( "maven-archetype", "jar" );
    }

    public String getExtensionForPackaging( String packaging )
    {
        if ( typeToExtensions.containsKey( packaging ) )
        {
            return typeToExtensions.get( packaging );
        }
        else
        {
            // default's to packaging name, ie. "jar", "war", "pom", etc.
            return packaging;
        }
    }

}
