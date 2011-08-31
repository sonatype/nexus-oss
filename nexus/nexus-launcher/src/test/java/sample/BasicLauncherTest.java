package sample;

import org.sonatype.nexus.bundle.launcher.NexusBundleLauncher;
import javax.inject.Inject;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.sonatype.nexus.bundle.NexusBundleConfiguration;
import org.sonatype.nexus.test.ConfigurableInjectedTest;

/**
 *
 */
public class BasicLauncherTest extends ConfigurableInjectedTest{

    @Inject
    private Logger logger;

    @Inject
    private NexusBundleLauncher nexusBundleLauncher;

    @Inject
    private NexusBundleConfiguration.Builder nexusBuilder;

    @Before
    public void before() {
        assertThat(logger, notNullValue());
        logger.debug(testName.getMethodName());
        assertThat(nexusBundleLauncher, notNullValue());
    }

    @After
    public void after() {
    }

    @Test
    public void testBundleService(){
        String nexusOSSArtifactCoords = "org.sonatype.nexus:nexus-oss-webapp:tar.gz:bundle:1.9.3-SNAPSHOT";
        NexusBundleConfiguration config = new NexusBundleConfiguration.Builder(nexusOSSArtifactCoords, "mybundle").build();
        config = nexusBuilder.setBundleId("nexus1").build();
        config = nexusBuilder.setBundleId("nexus2").build();
        assertThat(config.getBundleArtifactCoordinates(), is("org.sonatype.nexus:nexus-oss-webapp:tar.gz:bundle:1.9.3-SNAPSHOT"));
        //ManagedNexusBundle bundle = nexusBundleLauncher.start(config);
        //nexusBundleLauncher.stop(bundle);
    }

}
