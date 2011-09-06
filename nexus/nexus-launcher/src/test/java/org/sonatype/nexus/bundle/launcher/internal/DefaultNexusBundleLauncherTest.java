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
package org.sonatype.nexus.bundle.launcher.internal;

import java.io.File;
import java.util.List;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.nexus.bundle.launcher.util.ArtifactResolver;
import org.sonatype.nexus.bundle.launcher.util.NexusLauncherUtils;
import org.sonatype.nexus.bundle.launcher.util.PortReservationService;

/**
 * This is just a dummy test for now
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultNexusBundleLauncherTest {

    @Mock
    private ArtifactResolver artifactResolver;

    @Mock
    private PortReservationService portService;

    @Mock
    private AntHelper ant;

    @Mock
    private NexusLauncherUtils bundleUtils;

    @Mock
    private File serviceWorkDir;

    @Mock
    private File overlaysSourceDir;

    @Mock
    private File fakeBundle;

    @Mock
    private List<String> bundleExcludes;


    private DefaultNexusBundleLauncher getLauncher(){
        return new DefaultNexusBundleLauncher(artifactResolver, portService, ant, bundleUtils, serviceWorkDir, overlaysSourceDir);
    }

    @Test
    public void nothing(){
        
    }

}
