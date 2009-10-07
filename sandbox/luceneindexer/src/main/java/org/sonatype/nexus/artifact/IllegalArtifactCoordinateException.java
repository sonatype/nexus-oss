/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

/**
 * Thrown if was provided an illegal artifact coordinate
 * 
 * @author juven
 */
public class IllegalArtifactCoordinateException
    extends Exception
{
    private static final long serialVersionUID = 7137593998855995199L;

    public IllegalArtifactCoordinateException( String message )
    {
        super( message );
    }

    public IllegalArtifactCoordinateException( String message, Throwable throwable )
    {
        super( message, throwable );
    }
}
