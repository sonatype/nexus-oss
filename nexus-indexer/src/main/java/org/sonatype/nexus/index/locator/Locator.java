/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
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

/**
 * Locate an repository "elements" relative to some file. Always is the file that makes "relative to". So, if you are
 * looking for file SHA1 checksum, than pass the file to Sha1Locator.
 * 
 * @author Jason van Zyl
 */
public interface Locator
{
    String ROLE = Locator.class.getName();

    File locate( File source );
}
