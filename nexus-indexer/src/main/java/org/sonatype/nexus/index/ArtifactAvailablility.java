/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.index;

public enum ArtifactAvailablility
{
    /**
     * Artifact is not present locally
     */
    NOT_PRESENT( 0 ),

    /**
     * Artifact is present locally
     */
    PRESENT( 1 ),

    /**
     * Artifact is not available
     */
    NOT_AVAILABLE( 2 );

    private final int n;

    private ArtifactAvailablility( int n )
    {
        this.n = n;
    }

    @Override
    public String toString()
    {
        return Integer.toString( n );
    }

    public static ArtifactAvailablility fromString( String s )
    {
        try
        {
            switch ( Integer.parseInt( s ) )
            {
                case 1:
                    return PRESENT;
                case 2:
                    return NOT_AVAILABLE;
                default:
                    return NOT_PRESENT;
            }
        }
        catch ( NumberFormatException ex )
        {
            return NOT_PRESENT;
        }
    }
}
