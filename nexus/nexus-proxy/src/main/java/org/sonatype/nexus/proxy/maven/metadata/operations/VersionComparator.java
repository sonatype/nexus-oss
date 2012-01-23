/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.util.Comparator;

/**
 * version comparator used elsewhere to keep version collections sorted
 *
 * @author Oleg Gusakov
 * @version $Id: VersionComparator.java 744245 2009-02-13 21:23:44Z hboutemy $
 */
public class VersionComparator
    implements Comparator<String>
{

    public int compare( String v1, String v2 )
    {
        if ( v1 == null || v2 == null )
        {
            throw new IllegalArgumentException();
        }

        ComparableVersion av1 = new ComparableVersion( v1 );
        ComparableVersion av2 = new ComparableVersion( v2 );

        return av1.compareTo( av2 );
    }

}
