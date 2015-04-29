package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

/**
 * groupId-artifactId-baseVersion VO.
 *
 * @since 3.0
 */
public class GAbV
{
  private final String groupId;

  private final String artifactId;

  private final String baseVersion;

  public GAbV(final String groupId, final String artifactId, final String baseVersion) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.baseVersion = baseVersion;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getBaseVersion() {
    return baseVersion;
  }
}
