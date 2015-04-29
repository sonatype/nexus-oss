package org.sonatype.nexus.repository.storage;

import java.util.ListIterator;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Context of {@link Processor}s, used to share some common attributes and Tx.
 *
 * @since 3.0
 */
public class ProcessorContext
    extends ComponentSupport
{
  /**
   * Custom attributes to configure backing and logging.
   */
  private static class Attributes
      extends AttributesMap
  {
    public Attributes() {
      super(Maps.<String, Object>newHashMap());
    }
  }

  private final Attributes attributes;

  private final Supplier<StorageTx> storageTxSupplier;

  private final ListIterator<Processor> processors;

  public ProcessorContext(final Supplier<StorageTx> storageTxSupplier,
                          final ListIterator<Processor> processors)
  {
    this.storageTxSupplier = checkNotNull(storageTxSupplier);
    this.processors = checkNotNull(processors);
    this.attributes = new Attributes();
  }

  public AttributesMap getAttributes() {
    return attributes;
  }

  public Supplier<StorageTx> getStorageTxSupplier() {
    return storageTxSupplier;
  }

  public void proceed() {
    if (!processors.hasNext()) {
      log.debug("Processors exhausted");
    }
    final Processor processor = processors.next();
    try {
      log.debug("Proceeding to processor: {}", processor);
      processor.process(this);
    }
    finally {
      if (processors.hasPrevious()) {
        processors.previous();
      }
    }
  }
}
