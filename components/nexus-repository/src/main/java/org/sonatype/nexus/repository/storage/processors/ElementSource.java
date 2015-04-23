package org.sonatype.nexus.repository.storage.processors;

import org.sonatype.nexus.repository.storage.ProcessorContext;

/**
 * Element source, that sources elements for {@link Loop}.
 *
 * @since 3.0
 */
public abstract class ElementSource<T>
{
  public abstract void addToContext(final ProcessorContext context, final T element);

  public abstract void removeFromContext(final ProcessorContext context, final T element);

  public abstract Iterable<T> elements(final ProcessorContext context);
}
