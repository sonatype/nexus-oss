package org.sonatype.nexus.bundle;

import java.util.Collection;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import static org.sonatype.nexus.bundle.NexusBundleConfiguration.Builder;

/**
 *
 * @author plynch
 */
public class NexusBundleConfigurationTest {

    private Builder testBuilder = new Builder("org.sonatype.nexus:nxus-oss-webapp:bundle:zip", "mybundle").setConfigureOptionalPlugins(true).setConfigurePluginWebapps(true).setLicensed(true).addBundleExcludes("**/foo", "**/bar").addPluginCoordinates("org.foo:plugin", "com.foo:plugin");

    private void assertDefaultConfig(NexusBundleConfiguration config){
        assertThat(config.getBundleId(), is("mybundle"));
        assertThat(config.getBundleArtifactCoordinates(), is("org.sonatype.nexus:nxus-oss-webapp:bundle:zip"));
        assertThat(config.getNexusBundleExcludes(), hasItems("**/foo", "**/bar"));
        assertThat(config.getPluginCoordinates(), hasItems("org.foo:plugin", "com.foo:plugin"));
        assertThat(config.isLicensed(), is(true));
        assertThat(config.isConfigureOptionalPlugins(), is(true));
        assertThat(config.isConfigurePluginWebapps(), is(true));
    }

    @Test
    public void builderConfiguresAllValues() {
        NexusBundleConfiguration config = testBuilder.build();
        assertDefaultConfig(config);
    }

    @Test
    public void builderCopyConstructorCopiesAllValues() {
        Builder copy = new Builder(testBuilder);
        NexusBundleConfiguration config = copy.build();
        assertDefaultConfig(config);
    }

    // here as type safety hack only, empty() by itself does not seem to work
    private static final Matcher<Collection<String>> emptyStringCollection = empty();

    @Test
    public void validateDefaultValues(){
        NexusBundleConfiguration config = new Builder("coordinate", "id").build();
        assertThat(config.getBundleId(), is("id"));
        assertThat(config.getBundleArtifactCoordinates(), is("coordinate"));
        assertThat(config.getNexusBundleExcludes(), emptyStringCollection);
        assertThat(config.getPluginCoordinates(), emptyStringCollection);
        assertThat(config.isLicensed(), is(false));
        assertThat(config.isConfigureOptionalPlugins(), is(false));
        assertThat(config.isConfigurePluginWebapps(), is(false));

    }
    
}
