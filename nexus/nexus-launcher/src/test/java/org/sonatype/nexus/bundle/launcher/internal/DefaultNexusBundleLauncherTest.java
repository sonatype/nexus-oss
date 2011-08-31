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
    private File serviceWorkDir;

    @Mock
    private File overlaysSourceDir;

    @Mock
    private File fakeBundle;

    @Mock
    private List<String> bundleExcludes;


    private DefaultNexusBundleLauncher getLauncher(){
        return new DefaultNexusBundleLauncher(artifactResolver, portService, ant, serviceWorkDir, overlaysSourceDir);
    }

    @Test(expected=NullPointerException.class)
    public void extractBundleArg1Null() {
        assertThat(serviceWorkDir, notNullValue());
        assertThat(bundleExcludes, notNullValue());
        getLauncher().extractBundle(null, serviceWorkDir, bundleExcludes);
    }

    @Test(expected=NullPointerException.class)
    public void extractBundleArg2Null() {
        assertThat(fakeBundle, notNullValue());
        assertThat(bundleExcludes, notNullValue());
        getLauncher().extractBundle(fakeBundle, null, bundleExcludes);
    }

    @Test(expected=NullPointerException.class)
    public void extractBundleArg3Null() {
        assertThat(fakeBundle, notNullValue());
        assertThat(serviceWorkDir, notNullValue());
        getLauncher().extractBundle(fakeBundle, serviceWorkDir, null);
    }


}
