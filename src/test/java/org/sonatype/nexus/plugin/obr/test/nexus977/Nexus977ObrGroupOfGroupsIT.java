/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.obr.test.nexus977;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.plugin.obr.test.AbstractObrDownloadIT;
import org.testng.annotations.Test;


public class Nexus977ObrGroupOfGroupsIT
    extends AbstractObrDownloadIT
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        copy( "o1", "org/apache/felix/org.apache.felix.webconsole/3.0.0/org.apache.felix.webconsole-3.0.0.jar" );
        copy( "o2", "org/apache/felix/org.osgi.compendium/1.4.0/org.osgi.compendium-1.4.0.jar" );
        copy( "o3", "org/apache/geronimo/specs/geronimo-servlet_3.0_spec/1.0/geronimo-servlet_3.0_spec-1.0.jar" );
        copy( "o4", "org/apache/portals/portlet-api_2.0_spec/1.0/portlet-api_2.0_spec-1.0.jar" );
    }

    private void copy( final String dest, final String file )
        throws IOException
    {
        FileUtils.copyFile( new File( FELIX_REPO, file ), new File( nexusWorkDir, "storage/" + dest + "/" + file ) );
    }

    @Test
    public void obrGroupOfGroups()
        throws Exception
    {
        downloadApacheFelixWebManagement( "g1" );
    }

}
