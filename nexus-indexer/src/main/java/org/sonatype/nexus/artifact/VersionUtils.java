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
package org.sonatype.nexus.artifact;

import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.sonatype.nexus.DefaultNexusEnforcer;
import org.sonatype.nexus.NexusEnforcer;

public class VersionUtils
{
    private static NexusEnforcer enforcer = new DefaultNexusEnforcer();
    // Note that there is an 'OR' to support 2 different patterns.
    // i.e. the proper way 1.0-20080707.124343
    // i.e. the newly supported way 20080707.124343 (no base version, i.e. 1.0)
    private static final Pattern VERSION_FILE_PATTERN = 
        Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$|^([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    private static final Pattern STRICT_VERSION_FILE_PATTERN = 
        Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    
    public static boolean isSnapshot( String baseVersion )
    {
        if ( enforcer.isStrict() )
        {
            synchronized ( STRICT_VERSION_FILE_PATTERN )
            {
                return STRICT_VERSION_FILE_PATTERN.matcher( baseVersion ).matches()
                || baseVersion.endsWith( Artifact.SNAPSHOT_VERSION );   
            }
        }
        else
        {
            synchronized ( VERSION_FILE_PATTERN )
            {
                return VERSION_FILE_PATTERN.matcher( baseVersion ).matches()
                || baseVersion.endsWith( Artifact.SNAPSHOT_VERSION );   
            }
        }
    }
}
