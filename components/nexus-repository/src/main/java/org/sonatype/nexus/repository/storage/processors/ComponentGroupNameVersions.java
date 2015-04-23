package org.sonatype.nexus.repository.storage.processors;

import java.util.List;

import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

/**
 * Distinct component versions source for a given group and name. Requires {@link StorageFacet#P_GROUP} and {@link
 * StorageFacet#P_NAME} in context.
 *
 * @since 3.0
 */
public class ComponentGroupNameVersions
    extends StringElementSource
{
  public ComponentGroupNameVersions() {
    super(StorageFacet.P_VERSION);
  }

  @Override
  public List<String> elements(final ProcessorContext context) {
    final String group = context.getAttributes().require(StorageFacet.P_GROUP, String.class);
    final String name = context.getAttributes().require(StorageFacet.P_NAME, String.class);
    final List<String> versions = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL("select distinct(version) as val from component where group=? and name=? limit -1"))
          .execute(group, name);
      for (ODocument doc : docs) {
        final String docVal = doc.field("val", OType.STRING);
        versions.add(docVal);
      }

    }
    return versions;
  }
}
