package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import java.util.List;
import java.util.Set;

import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.processors.ValueElementSource;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

/**
 * Distinct baseVersion source for a given groupId (group) and artifactId (name). Requires {@link
 * StorageFacet#P_GROUP} and {@link StorageFacet#P_NAME} in context. This element source is {@link Maven2Format}
 * specific!
 *
 * @since 3.0
 */
public class GAbVsElementSource
    extends ValueElementSource<GAbVs>
{
  public GAbVsElementSource() {
    super(GAbVs.class);
  }

  @Override
  public List<GAbVs> elements(final ProcessorContext context) {
    final List<GAbVs> versions = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL(
              "select group as groupId, name as artifactId, set(attributes.maven2.baseVersion) as baseVersions from component group by group, name limit=-1")) //  where bucket=?
              // TODO: bucket.rid!
          .execute();//tx.getBucket());
      for (ODocument doc : docs) {
        final String groupId = doc.field("groupId", OType.STRING);
        final String artifactId = doc.field("artifactId", OType.STRING);
        final Set<String> baseVersions = doc.field("baseVersions", OType.EMBEDDEDSET);
        versions.add(new GAbVs(groupId, artifactId, baseVersions));
      }
     }
    return versions;
  }
}
