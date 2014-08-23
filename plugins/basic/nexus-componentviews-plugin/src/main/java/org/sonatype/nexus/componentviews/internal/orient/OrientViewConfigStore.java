package org.sonatype.nexus.componentviews.internal.orient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigIdentity;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.ORecordMetadata;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named
@EagerSingleton
public class OrientViewConfigStore
    extends LifecycleSupport
    implements ViewConfigStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RecordIdObfuscator recordIdObfuscator;

  private final UserSuppliedConfigEntityAdapter entityAdapter = new UserSuppliedConfigEntityAdapter();

  private OClass entityType;

  @Inject
  public OrientViewConfigStore(final @Named("config") Provider<DatabaseInstance> databaseInstance,
                               final RecordIdObfuscator recordIdObfuscator)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      entityType = entityAdapter.register(db);
    }
  }

  @Override
  protected void doStop() throws Exception {
    entityType = null;
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  private ViewConfigIdentity convertId(final ORID rid) {
    String encoded = recordIdObfuscator.encode(entityType, rid);
    return new ViewConfigIdentity(encoded);
  }

  private ORID convertId(final ViewConfigIdentity id) {
    return recordIdObfuscator.decode(entityType, id.toString());
  }

  @Override
  public ViewConfigIdentity add(final ViewConfig item) throws IOException {
    ORID rid;
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = entityAdapter.create(db, item);
      rid = doc.getIdentity();
    }

    log.debug("Added item with RID: {}", rid);
    return convertId(rid);
  }

  @Override
  public void update(final ViewConfigIdentity id, final ViewConfig item) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // load record and apply updated item attributes
      ODocument doc = db.getRecord(rid);
      if (doc == null) {
        log.debug("Unable to update item with RID: {}", rid);
        throw new IllegalArgumentException("ViewConfigIdentity " + id + " is not present in the store.");
      }
      entityAdapter.write(doc, item);
    }

    log.debug("Updated item with RID: {}", rid);
  }

  @Override
  public void remove(final ViewConfigIdentity id) throws IOException {
    ORID rid = convertId(id);

    try (ODatabaseDocumentTx db = openDb()) {
      // if we can't get the metadata, then abort
      ORecordMetadata md = db.getRecordMetadata(rid);
      if (md == null) {
        log.debug("Unable to delete item with RID: {}", rid);
        return;
      }
      // else delete the record
      db.delete(rid);
    }

    log.debug("Deleted item with RID: {}", rid);
  }

  @Override
  public Map<ViewConfigIdentity, ViewConfig> getAll() throws IOException {

    Map<ViewConfigIdentity, ViewConfig> items = Maps.newHashMap();

    try (ODatabaseDocumentTx db = openDb()) {
      for (ODocument doc : entityAdapter.browse(db)) {
        ORID rid = doc.getIdentity();
        ViewConfig item = entityAdapter.read(doc);
        items.put(convertId(rid), item);
      }
    }

    return items;
  }

  @Override
  public ViewConfig get(final String viewName) {
    try (ODatabaseDocumentTx db = openDb()) {

      OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
          "SELECT FROM " + UserSuppliedConfigEntityAdapter.DB_CLASS + " WHERE viewName = ?");

      final List<ODocument> results = db.command(query).execute(viewName);

      if (results.isEmpty()) {
        return null;
      }

      return entityAdapter.read(results.get(0));
    }
  }
}
