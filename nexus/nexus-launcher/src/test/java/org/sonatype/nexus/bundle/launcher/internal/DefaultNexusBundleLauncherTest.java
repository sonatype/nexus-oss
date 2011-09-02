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
