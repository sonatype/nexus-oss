package org.sonatype.nexus.componentviews;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.componentviews.responses.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.0
 */
public class View
{
  private static final Logger log = LoggerFactory.getLogger(View.class);

  private final String name;

  private final Router router;

  private final Map<String, Handler> handlers;

  private final Handler noRouteHandler;

  public View(final String name, final Router router, final Map<String, Handler> handlers,
              final Handler noRouteHandler)
  {
    this.name = name;
    this.router = router;
    this.handlers = new HashMap<>(handlers);
    this.noRouteHandler = noRouteHandler;
  }

  public String getName() {
    return name;
  }

  public Response dispatch(final Request request) {
    final RequestMatcher requestMatcher = router.findRoute(request);

    if (requestMatcher == null) {
      return noRouteHandler.handle(request);
    }

    final String handlerChainName = requestMatcher.getHandlerName();

    final Handler handler = handlers.get(handlerChainName);
    if (handler == null) {
      log.error(
          "View {} route dispatched path {} to missing handler {}.referenced missing handler {} in response to path {}.",
          name, request.getPath(), handlerChainName);
      throw new IllegalStateException();
    }

    // No exception handling here presumes that these will get caught by a global filter and nicely reported as
    // 500 errors.
    return handler.handle(request);
  }
}
