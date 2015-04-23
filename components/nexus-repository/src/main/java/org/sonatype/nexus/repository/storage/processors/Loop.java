package org.sonatype.nexus.repository.storage.processors;

import java.util.List;

import org.sonatype.nexus.repository.storage.Processor;
import org.sonatype.nexus.repository.storage.ProcessorContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Looping processor.
 *
 * @since 3.0
 */
public class Loop<T>
    extends Processor
{
  private final ElementSource<T> elementSource;

  public Loop(final ElementSource<T> elementSource) {
    this.elementSource = checkNotNull(elementSource);
  }

  @Override
  public void process(final ProcessorContext context) {
    final Iterable<T> elements = elementSource.elements(context);
    for (T element : elements) {
      elementSource.addToContext(context, element);
      context.proceed();
      elementSource.removeFromContext(context, element);
    }
  }
}
