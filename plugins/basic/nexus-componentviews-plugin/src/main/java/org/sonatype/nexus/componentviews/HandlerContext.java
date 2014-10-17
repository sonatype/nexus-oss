package org.sonatype.nexus.componentviews;

import java.util.List;
import java.util.ListIterator;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Supports processing of a {@link ViewRequest} as it proceeds along a chain of {@link Handler}s.
 *
 * @since 3.0
 */
public class HandlerContext
    extends ComponentSupport
{
  private final ViewRequest request;

  private final ListIterator<Handler> handlers;

  public HandlerContext(final ViewRequest request, final List<Handler> handlers) {
    this.request = checkNotNull(request);
    this.handlers = checkNotNull(handlers).listIterator();
  }

  public ViewRequest getRequest() {
    return request;
  }

  /**
   * Invokes the next handler in the handler chain. Interceptor-style handlers should invoke this from their {@link
   * Handler#handle(HandlerContext)} method and return the result.
   */
  public ViewResponse proceed() throws Exception {
    log.debug("Proceed");

    if (handlers.hasNext()) {
      Handler handler = handlers.next();
      log.debug("Handler: {}", handler);

      try {
        return handler.handle(this);
      }
      finally {
        if (handlers.hasPrevious()) {
          handlers.previous();
        }
      }
    }
    else {
      throw new Exception("Unable to proceed");
    }
  }
}
