package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import java.util.List;

import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.storage.Processor;
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
public class GAbVExpando
    extends Processor
{
  @Override
  public void process(final ProcessorContext context) {
    final GAbV gabv = context.getAttributes().get(GAbV.class);
    context.getAttributes().set(StorageFacet.P_GROUP, gabv.getGroupId());
    context.getAttributes().set(StorageFacet.P_NAME, gabv.getArtifactId());
    context.getAttributes().set(StorageFacet.P_VERSION, gabv.getBaseVersion());
    super.process(context);
    context.getAttributes().remove(StorageFacet.P_VERSION);
    context.getAttributes().remove(StorageFacet.P_NAME);
    context.getAttributes().remove(StorageFacet.P_GROUP);
  }
}
