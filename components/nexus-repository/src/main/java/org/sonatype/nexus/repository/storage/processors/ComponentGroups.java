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
 * Distinct component groups source. No requirements for context.
 *
 * @since 3.0
 */
public class ComponentGroups
    extends StringElementSource
{
  public ComponentGroups() {
    super(StorageFacet.P_GROUP);
  }

  @Override
  public List<String> elements(final ProcessorContext context) {
    final List<String> groups = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      final Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL("select distinct(group) as val from component limit -1"))
          .execute();
      for (ODocument doc : docs) {
        final String docVal = doc.field("val", OType.STRING);
        groups.add(docVal);
      }

    }
    return groups;
  }
}
