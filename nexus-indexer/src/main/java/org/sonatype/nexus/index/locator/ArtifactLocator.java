/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.locator;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;

/**
 * Artifact locator. DO NOT USE IT!
 * 
 * @author Jason van Zyl
 * @deprecated There is no 100% reliable way to go from pom to artifact unless we are parsing the POM, taking the
 *             packaging, doing packaging to extension mapping (which is again not complete solution coz of custom
 *             packagings)... (cstamas)
 */
public class ArtifactLocator
    implements GavHelpedLocator
{
    public File locate( File source, GavCalculator gavCalculator, Gav gav )
    {
        return new File( source.getAbsolutePath().replaceAll( ".pom", ".jar" ) );
    }
}
