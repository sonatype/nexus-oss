package org.sonatype.nexus.bundle.launcher.util;

import java.io.File;
import java.util.Map;

/**
 *
 * @author plynch
 */
public interface ResolvedArtifact {

    public String getGroupId();

    public String getArtifactId();

    public String getVersion();

    public String getBaseVersion();

    public boolean isSnapshot();

    public String getClassifier();

    public String getExtension();

    public File getFile();

    public String getProperty(String key, String defaultValue);

    public Map<String, String> getProperties();
}
