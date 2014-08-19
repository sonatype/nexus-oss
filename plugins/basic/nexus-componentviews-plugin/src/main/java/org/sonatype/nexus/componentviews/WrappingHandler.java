package org.sonatype.nexus.componentviews;

/**
 * A Handler that wraps another handler, adding functionality, filtering requests or responses.
 *
 * @since 3.0
 */
public abstract class WrappingHandler
    implements Handler
{
  private final Handler innerHandler;

  public WrappingHandler(final Handler innerHandler) {
    this.innerHandler = innerHandler;
  }

  protected Handler getInnerHandler() {
    return innerHandler;
  }
}
