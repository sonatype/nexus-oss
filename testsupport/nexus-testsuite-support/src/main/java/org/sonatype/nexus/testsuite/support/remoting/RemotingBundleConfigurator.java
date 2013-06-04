package org.sonatype.nexus.testsuite.support.remoting;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusITSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Configures Nexus bundles for groovy-remote control support.
 *
 * @since 2.6
 */
public class RemotingBundleConfigurator
{
    private final NexusITSupport test;

    private Integer port;

    public RemotingBundleConfigurator(final NexusITSupport test) {
        this.test = checkNotNull(test);
    }

    public RemotingBundleConfigurator setPort(final Integer port) {
        this.port = port;
        return this;
    }

    public NexusBundleConfiguration configure(final NexusBundleConfiguration config) {
        checkState(port != null, "Missing port");

        return config.setLogLevel("org.sonatype.nexus.groovyremote", "DEBUG")
            // configure port for groovy-remote plugin
            .setSystemProperty("nexus.groovyremote.port", String.valueOf(port))
            .addPlugins(
                // install groovy-remote plugin
                test.artifactResolver().resolvePluginFromDependencyManagement(
                    "org.sonatype.nexus.plugins", "nexus-groovyremote-plugin"
                )
            );
    }
}
