package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import java.util.List;

import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.processors.ValueElementSource;

import com.google.common.collect.Lists;

/**
 * Distinct baseVersion source for a given groupId (group) and artifactId (name). Requires {@link
 * StorageFacet#P_GROUP} and {@link StorageFacet#P_NAME} in context. This element source is {@link Maven2Format}
 * specific!
 *
 * @since 3.0
 */
public class GAbVElementSource
    extends ValueElementSource<GAbV>
{
  public GAbVElementSource() {
    super(GAbV.class);
  }

  @Override
  public List<GAbV> elements(final ProcessorContext context) {
    final GAbVs gabvs = context.getAttributes().require(GAbVs.class);
    final List<GAbV> versions = Lists.newArrayList();
    for (String baseVersion : gabvs.getBaseVersions()) {
      versions.add(new GAbV(gabvs.getGroupId(), gabvs.getArtifactId(), baseVersion));
    }
    return versions;
  }
}
