/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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

import org.sonatype.nexus.proxy.maven.version.GenericVersionParser;
import org.sonatype.nexus.proxy.maven.version.InvalidVersionSpecificationException;
import org.sonatype.nexus.proxy.maven.version.Version;
import org.sonatype.nexus.proxy.maven.version.VersionParser;

/**
 * version comparator used elsewhere to keep version collections sorted
 * 
 * @author Oleg Gusakov
 * @version $Id: VersionComparator.java 744245 2009-02-13 21:23:44Z hboutemy $
 */
public class VersionComparator
    implements Comparator<String>
{
    private final VersionParser versionParser;

    public VersionComparator()
    {
        this.versionParser = new GenericVersionParser();
    }

    public int compare( final String v1, final String v2 )
    {
        if ( v1 == null || v2 == null )
        {
            throw new IllegalArgumentException();
        }

        final Version av1 = parseVersion( v1 );
        final Version av2 = parseVersion( v2 );

        return av1.compareTo( av2 );
    }

    protected Version parseVersion( final String v )
    {
        try
        {
            return versionParser.parseVersion( v );
        }
        catch ( InvalidVersionSpecificationException e )
        {
            // from implementation (coming from Aether) we know today this will never happen
            // but is here probably for future. Also, we do not deal with ranges, only single version strings
            return null;
        }
    }

}
