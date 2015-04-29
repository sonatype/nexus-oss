package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import java.util.List;

import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.processors.StringElementSource;

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
public class ComponentGroupNameBaseVersions
    extends StringElementSource
{
  public ComponentGroupNameBaseVersions() {
    super(StorageFacet.P_VERSION);
  }

  @Override
  public List<String> elements(final ProcessorContext context) {
    final String group = context.getAttributes().require(StorageFacet.P_GROUP, String.class);
    final String name = context.getAttributes().require(StorageFacet.P_NAME, String.class);
    final List<String> versions = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL(
              "select distinct(attributes.maven2.baseVersion) as val from component where group=? and name=? limit -1")) // bucket=? and
              // TODO: bucket.rid!
          .execute(group, name); // tx.getBucket(),
      for (ODocument doc : docs) {
        final String docVal = doc.field("val", OType.STRING);
        versions.add(docVal);
      }

    }
    return versions;
  }
}
