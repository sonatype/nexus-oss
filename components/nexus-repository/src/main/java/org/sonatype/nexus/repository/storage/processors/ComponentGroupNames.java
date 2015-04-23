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
 * Distinct component names source for a given group. Requires {@link StorageFacet#P_GROUP} in context.
 *
 * @since 3.0
 */
public class ComponentGroupNames
    extends StringElementSource
{
  public ComponentGroupNames() {
    super(StorageFacet.P_NAME);
  }

  @Override
  public List<String> elements(final ProcessorContext context) {
    final String group = context.getAttributes().require(StorageFacet.P_GROUP, String.class);
    final List<String> names = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL("select distinct(name) as val from component where group=? limit -1"))
          .execute(group);
      for (ODocument doc : docs) {
        final String docVal = doc.field("val", OType.STRING);
        names.add(docVal);
      }

    }
    return names;
  }
}
