/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

/**
 * An abstraction for scanning content of the artifact repositories
 *  
 * @author Jason van Zyl
 */
public interface Scanner
{
    /**
     * Scan repository artifacts and populate {@link ScanningResult} 
     */
    ScanningResult scan( ScanningRequest request );
}
