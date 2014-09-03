package org.sonatype.nexus.componentviews;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.componentviews.config.ViewConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A binding between a view name (which is part of the URL), and a list of {@link RequestMatcher request matchers},
 * each of which can delegate to a chain of {@link Handler}.
 *
 * @since 3.0
 */
public class View
{
  private static final Logger log = LoggerFactory.getLogger(View.class);

  private final String name;

  private final Router router;

  private final Map<String, Handler> handlers;

  private final Handler noRouteHandler;

  private final ViewConfig config;

  public View(final String name, final Router router, final Map<String, Handler> handlers,
              final Handler noRouteHandler, final ViewConfig config)
  {
    this.name = name;
    this.router = router;
    this.config = config;
    this.handlers = new HashMap<>(handlers);
    this.noRouteHandler = noRouteHandler;
  }

  public String getName() {
    return name;
  }

  public ViewResponse dispatch(final ViewRequest request) {
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

  public ViewConfig getConfig() {
    return config;
  }
}
