package org.sonatype.nexus.repository.storage.processors;

import org.sonatype.nexus.repository.storage.ProcessorContext;

/**
 * Element source that uses {@link String} elements.
 *
 * @since 3.0
 */
public abstract class StringElementSource
    extends ElementSource<String>
{
  private final String contextKey;

  public StringElementSource(final String contextKey) {
    this.contextKey = contextKey;
  }

  @Override
  public void addToContext(final ProcessorContext context, final String element) {
    context.getAttributes().set(contextKey, element);
  }

  @Override
  public void removeFromContext(final ProcessorContext context, final String element) {
    context.getAttributes().remove(contextKey);
  }
}
