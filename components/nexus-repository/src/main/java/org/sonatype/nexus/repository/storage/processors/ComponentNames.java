package org.sonatype.nexus.repository.storage.processors;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

/**
 * Distinct component names source with custom where clause. No requirements for context. This source is prone to OOM if
 * where clause is not selective, would load up whole world.
 *
 * @since 3.0
 */
public class ComponentNames
    extends StringElementSource
{
  @Nullable
  private final String whereClause;

  @Nullable
  private final Map<String, Object> parameters;

  public ComponentNames(@Nullable String whereClause,
                        @Nullable Map<String, Object> parameters)
  {
    super(StorageFacet.P_NAME);
    this.whereClause = whereClause;
    this.parameters = parameters;
  }

  @Override
  public List<String> elements(final ProcessorContext context) {
    final List<String> names = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      final Iterable<ODocument> docs = tx.getDb()
          .command(new OCommandSQL("select distinct(name) as val from component " + whereClause + " limit -1"))
          .execute(parameters);
      for (ODocument doc : docs) {
        final String docVal = doc.field("val", OType.STRING);
        names.add(docVal);
      }

    }
    return names;
  }
}
