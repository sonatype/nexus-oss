package org.sonatype.nexus.plugins;

/**
 * A simple descriptor for Nexus plugins.
 * <p>
 * TODO: These stuff should mainly come from POM (why to keep 'em duplicated?) using some tooling. So, for example we
 * should create a nexus-plugin packaging for maven (a maven packaging plugin) that will simply sneak-in a NexusPlugin
 * implementation and put it into the JAR, that will be eventually picked up by Nexus.
 * 
 * @author cstamas
 */
public interface NexusPlugin
{
    String getGroupId();

    String getArtifactId();

    String getVersion();

    String getName();

    String getDescription();

    String getUrl();
}
