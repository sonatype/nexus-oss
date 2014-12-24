package org.sonatype.nexus.repository.storage;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link StorageFacet} implementation.
 */
@Named
public class StorageFacetImpl
  extends FacetSupport
  implements StorageFacet
{
  private final StorageService storage;

  @Inject
  public StorageFacetImpl(final StorageService storage) {
    this.storage = checkNotNull(storage);
  }

  @Override
  protected void doInit(final Repository repository) throws Exception {
    // TODO:
  }
}
