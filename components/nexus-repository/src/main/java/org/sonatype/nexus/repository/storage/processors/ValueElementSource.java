package org.sonatype.nexus.repository.storage.processors;

import org.sonatype.nexus.repository.storage.ProcessorContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Element source that uses value objects as elements.
 *
 * @since 3.0
 */
public abstract class ValueElementSource<T>
    extends ElementSource<T>
{
  private final Class<T> entityClass;

  public ValueElementSource(final Class<T> entityClass) {
    this.entityClass = checkNotNull(entityClass);
  }

  @Override
  public void addToContext(final ProcessorContext context, final T element) {
    context.getAttributes().set(entityClass, element);
  }

  @Override
  public void removeFromContext(final ProcessorContext context, final T element) {
    context.getAttributes().remove(entityClass);
  }
}
