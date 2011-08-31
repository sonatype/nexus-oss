package org.sonatype.nexus.bundle.launcher.util;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.sonatype.aether.artifact.Artifact;

/**
 * Immutable resolved artifact.
 * @since 1.9.3
 */
public class DefaultResolvedArtifact implements ResolvedArtifact{

    DefaultResolvedArtifact(final Artifact artifact){
        Preconditions.checkNotNull(artifact);
        this.artifact = artifact;
    }

    private Artifact artifact;

    @Override
    public String getGroupId() {
        return this.artifact.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return this.artifact.getArtifactId();
    }

    @Override
    public String getVersion() {
        return this.artifact.getVersion();
    }

    @Override
    public String getBaseVersion() {
        return this.artifact.getBaseVersion();
    }

    @Override
    public boolean isSnapshot() {
        return this.artifact.isSnapshot();
    }

    @Override
    public String getClassifier() {
        return this.artifact.getClassifier();
    }

    @Override
    public String getExtension() {
        return this.artifact.getExtension();
    }

    @Override
    public File getFile() {
        return this.artifact.getFile();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return this.artifact.getProperty(key, defaultValue);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(this.artifact.getProperties());
    }

}
