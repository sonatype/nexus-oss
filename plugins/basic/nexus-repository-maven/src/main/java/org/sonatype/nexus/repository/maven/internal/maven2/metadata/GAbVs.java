package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import java.util.Set;

/**
 * groupId-artifactId-baseVersions VO.
 *
 * @since 3.0
 */
public class GAbVs
{
  private final String groupId;

  private final String artifactId;

  private final Set<String> baseVersions;

  public GAbVs(final String groupId, final String artifactId, final Set<String> baseVersions) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.baseVersions = baseVersions;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public Set<String> getBaseVersions() {
    return baseVersions;
  }
}
