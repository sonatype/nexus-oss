/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

/**
 * An interface to calculate <code>Gav</code> based on provided artifact path and 
 * to calculate an artifact path from provided <code>Gav</code>.
 * 
 * @author Tamas Cservenak
 */
public interface GavCalculator
{
    Gav pathToGav( String path );

    String gavToPath( Gav gav );
}
