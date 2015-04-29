package org.sonatype.nexus.repository.storage;

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Basic empty processor.
 *
 * @since 3.0
 */
public abstract class Processor
    extends ComponentSupport
{
  public void process(final ProcessorContext context) {
    context.proceed();
  }
}
