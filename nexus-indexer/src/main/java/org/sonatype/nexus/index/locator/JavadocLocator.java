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
package org.sonatype.nexus.index.locator;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;

/** @author Jason van Zyl */
public class JavadocLocator
    implements Locator
{
    public File locate( File source, Gav gav )
    {
        return new File( source.getAbsolutePath().replaceAll( ".pom", "-javadoc.jar" ) );
    }
}
