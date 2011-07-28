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
package org.sonatype.nexus.plugins.migration.nexus1441;

import org.apache.maven.index.artifact.Gav;
import org.junit.Test;

public class Nexus1441DeployArtifactsIT
    extends AbstractDeployBridgeIT
{
    
    public Nexus1441DeployArtifactsIT()
    {

    }

    @Test
    public void mavenDeployRelease()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.maven", "maven-deploy-released", "1.0", null, "jar", null, null, null, false, false,
                     null, false, null );
        deploy( gav, "test-releases-local", true );
    }

    @Test
    public void mavenDeploySnapshot()
        throws Exception
    {
        Gav gav =
            new Gav( "nxcm280.maven", "maven-deployed-snapshot", "1.0-SNAPSHOT", null, "jar", null, null, null, true,
                     false, null, false, null );
        deploy( gav, "test-snapshots-local", false, 1 );
    }

}
