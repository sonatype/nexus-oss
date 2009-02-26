/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.locator;

import java.io.File;

/** 
 * A signature locator to locate the signature file relative to POM.
 * 
 * @author Jason van Zyl */
public class SignatureLocator
    implements Locator
{
    public File locate( File source )
    {
        // return new File( source.getParentFile(), source.getName() + ".asc" );
        return new File( source.getAbsolutePath() + ".asc" );
    }
}
