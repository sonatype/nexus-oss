package org.sonatype.nexus.repository.storage.processors;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

/**
 * Distinct component version source for a given name. Requires {@link StorageFacet#P_NAME} in context.
 *
 * @since 3.0
 */
public class ComponentNameVersions
    extends StringElementSource
{
  public ComponentNameVersions()
  {
    super(StorageFacet.P_VERSION);
  }

  @Override
  public List<String> elements(final ProcessorContext context) {
    final String name = context.getAttributes().require(StorageFacet.P_NAME, String.class);
    final List<String> versions = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      final Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL("select distinct(version) as val from component where name=? limit -1"))
          .execute(ImmutableMap.of("name", name));
      for (ODocument doc : docs) {
        final String docVal = doc.field("val", OType.STRING);
        versions.add(docVal);
      }

    }
    return versions;
  }
}
