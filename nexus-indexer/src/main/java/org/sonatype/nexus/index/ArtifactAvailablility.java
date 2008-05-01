/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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
