/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index.locator;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;

/**
 * Locate an artifactId given it's POM.
 *
 * @author Jason van Zyl
 */
public interface GavHelpedLocator
{
    String ROLE = GavHelpedLocator.class.getName();

    File locate( File source, GavCalculator gavCalculator, Gav gav );
}
