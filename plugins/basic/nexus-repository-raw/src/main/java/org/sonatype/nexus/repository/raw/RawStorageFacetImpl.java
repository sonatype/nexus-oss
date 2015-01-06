package org.sonatype.nexus.repository.raw;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.storage.BlobRef;
import org.sonatype.nexus.repository.storage.BlobStoreManager;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @since 3.0
 */
public class RawStorageFacetImpl
    extends FacetSupport
    implements RawStorageFacet
{
  public static final String CONTENT_TYPE_PROPERTY = "content_type";

  public static final String BLOB_REF_PROPERTY = "blob_ref";

  private final BlobStoreManager blobStoreManager;

  private String defaultBlobStore;

  @Inject
  public RawStorageFacetImpl(final BlobStoreManager blobStoreManager) {
    this.blobStoreManager = checkNotNull(blobStoreManager, "blob store manager");
  }

  @Override
  protected void doInit() throws Exception {
    final Configuration config = getRepository().getConfiguration();
    final NestedAttributesMap storageConfig = config.attributes("storage");

    defaultBlobStore = storageConfig.get("defaultBlobStore", String.class);
  }

  @Nullable
  @Override
  public RawContent get(final String path) throws IOException {
    return (RawContent) inTx(new GraphOperation()
    {
      @Override
      public Object execute(final OrientGraph graph, final StorageFacet storage) {
        final Vertex asset = storage.findByPath(graph, path);
        if (asset == null) {
          return null;
        }

        final BlobRef blobRef = getBlobRef(path, asset);
        final Blob blob = getBlob(blobRef);
        checkState(blob != null, "asset at path %s refers to missing blob %s", path, blobRef);

        return marshall(asset, blob);
      }
    });
  }

  @Nullable
  @Override
  public RawContent put(final String path, final RawContent content) throws IOException {
    return (RawContent) inTx(new GraphOperation()
    {
      @Override
      public Object execute(final OrientGraph graph, final StorageFacet storage) throws IOException {

        final Vertex asset = storage.createAsset(graph);

        // TODO: Figure out created-by header
        final ImmutableMap<String, String> headers = ImmutableMap
            .of(BlobStore.BLOB_NAME_HEADER, path, BlobStore.CREATED_BY_HEADER, "unknown");

        final BlobStore store = blobStoreManager.getStore(defaultBlobStore);

        final Blob blob = store.create(content.toPayload().openInputStream(), headers);

        final BlobRef blobRef = new BlobRef("NODE", defaultBlobStore, blob.getId().asUniqueString());

        asset.setProperty(BLOB_REF_PROPERTY, blobRef.toString());

        return marshall(asset, blob);
      }
    });
  }

  @Nullable
  @Override
  public boolean delete(final String path) throws IOException {
    return (boolean) inTx(new GraphOperation()
    {
      @Override
      public Object execute(final OrientGraph graph, final StorageFacet storage) {
        final Vertex asset = storage.findByPath(graph, path);
        if (asset == null) {
          return false;
        }

        final BlobRef blobRef = getBlobRef(path, asset);
        final boolean delete = deleteBlob(blobRef);
        if (!delete) {
          log.warn("Deleted asset {} referenced missing blob {}", path, blobRef);
        }

        storage.delete(graph, asset);

        return true;
      }
    });
  }

  private static interface GraphOperation
  {
    Object execute(OrientGraph graph, final StorageFacet storage) throws IOException;
  }

  private Object inTx(GraphOperation operation) throws IOException {
    final StorageFacet storage = getStorage();
    final OrientGraph graph = storage.getGraphTx();
    try {
      final Object result = operation.execute(graph, storage);
      graph.commit();
      return result;
    }
    catch (IOException | RuntimeException e) {
      graph.rollback();
      return Throwables.propagate(e);
    }
    finally {
      graph.shutdown();
    }
  }

  private StorageFacet getStorage() {
    return getRepository().facet(StorageFacet.class);
  }

  private BlobRef getBlobRef(final String path, final Vertex asset) {
    String blobRefStr = asset.getProperty(BLOB_REF_PROPERTY);
    checkState(blobRefStr != null, "asset at path %s has missing blob reference", path);
    return new BlobRef(blobRefStr);
  }

  private Blob getBlob(final BlobRef blobRef) {
    final BlobId blobId = blobRef.getBlobId();
    final BlobStore store = blobStoreManager.getStore(blobRef.getStore());
    return store.get(blobId);
  }

  private boolean deleteBlob(final BlobRef blobRef) {
    final BlobId blobId = blobRef.getBlobId();
    final BlobStore store = blobStoreManager.getStore(blobRef.getStore());
    return store.delete(blobId);
  }

  private RawContent marshall(Vertex asset, Blob blob) {
    return null;
  }
}
