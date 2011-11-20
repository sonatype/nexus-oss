/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.meclipse1299;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.contains;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.testng.annotations.Test;

public class MECLIPSE1299P2CompositeRepoMergeRulesIT
    extends AbstractNexusProxyP2IT
{

    public MECLIPSE1299P2CompositeRepoMergeRulesIT()
    {
        super( "meclipse1299" );
    }

    @Test
    public void test()
        throws Exception
    {
        final File artifactsXmlFile = new File( "target/downloads/meclipse1299/artifacts.xml" );
        if ( artifactsXmlFile.exists() )
        {
            assertThat( artifactsXmlFile.delete(), is( true ) );
        }

        downloadFile(
            new URL( getRepositoryUrl( getTestRepositoryId() ) + "/artifacts.xml" ),
            artifactsXmlFile.getAbsolutePath()
        );
        assertThat( artifactsXmlFile, exists() );

        assertThat( artifactsXmlFile, contains(
            "<mappings size=\"5\">",
            "<rule output=\"${repoUrl}/plugins/${id}_${version}.jar\" filter=\"(&amp; (classifier=osgi.bundle))\" />",
            "<rule output=\"${repoUrl}/binary/${id}_${version}\" filter=\"(&amp; (classifier=binary))\" />",
            "<rule output=\"${repoUrl}/features/${id}_${version}.jar\" filter=\"(&amp; (classifier=org.eclipse.update.feature))\" />",
            "<rule output=\"foo.bar\" filter=\"(&amp; (classifier=foo))\" />",
            "<rule output=\"bar.foo\" filter=\"(&amp; (classifier=bar))\" />"
        ) );
    }

}
