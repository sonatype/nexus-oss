package org.sonatype.nexus.blobstore.api;

import javax.inject.Inject;

import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.common.throwables.MultipleFailures;

import com.google.inject.assistedinject.Assisted;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link BlobStore} implementation.
 */
public class BlobStoreImpl
  extends StateGuardLifecycleSupport
  implements BlobStore
{
  //private final EventBus eventBus;

  private final Type type;

  private final FacetLookup facets = new FacetLookup();

  private BlobStoreConfiguration configuration;

  @Inject
  public BlobStoreImpl(final @Assisted Type type) {
    this.type = checkNotNull(type);
  }

  @Override
  public Type getType() {
    return type;
  }
  
  //TODO - KR add state guarding
  @Override
  public void attach(final Facet facet) throws Exception {
    checkNotNull(facet);
    log.debug("Attaching facet: {}", facet);
    facet.init(this);
    facets.add(facet);
  }

  @Override
  public <T extends Facet> T facet(final Class<T> type) throws IllegalStateException {
    T facet = facets.get(type);
    if (facet == null) {
      throw new IllegalStateException("Failed to find facet: " + type);
    }

    return facet;
  }

  @Override
  public void delete() throws Exception {
    MultipleFailures failures = new MultipleFailures();

    for (Facet facet : facets.reverse()) {
      try {
        log.debug("Deleting facet: {}", facet);
        facet.delete();
      }
      catch (Throwable t) {
        log.error("Failed to delete facet: {}", facet, t);
        failures.add(t);
      }
    }
    failures.maybePropagate("Failed to delete facets");
  }

  @Override
  public BlobStoreConfiguration getBlobStoreConfiguration() {
    return checkNotNull(configuration);
  }

  @Override
  public String getName() {
    return checkNotNull(configuration.getName());
  }

  @Override
  public void init(final BlobStoreConfiguration configuration) {
    this.configuration = checkNotNull(configuration);
  }

  @Override
  public void start() throws Exception {
    MultipleFailures failures = new MultipleFailures();
    for (Facet facet : facets) {
      try {
        log.debug("Starting facet: {}", facet);
        facet.start();
      }
      catch (Throwable t) {
        log.error("Failed to start facet: {}", facet, t);
        failures.add(t);
      }
    }
    failures.maybePropagate("Failed to start facets");

    // TODO - KR event publishing
  }

  @Override
  public void stop() throws Exception {
    MultipleFailures failures = new MultipleFailures();

    for (Facet facet : facets.reverse()) {
      try {
        log.debug("Stopping facet: {}", facet);
        facet.stop();
      }
      catch (Throwable t) {
        log.error("Failed to stop facet: {}", facet, t);
        failures.add(t);
      }
    }
    failures.maybePropagate("Failed to stop facets");

    // TODO - KR event publishing
  }
}
