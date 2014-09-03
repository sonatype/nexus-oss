package org.sonatype.nexus.componentviews.internal;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.web.ErrorStatusException;

import com.google.common.base.Throwables;

/**
 * The central servlet of the view framework, which dispatches incoming requests to the views available in the {@link
 * ViewRegistry}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentViewServlet
    extends HttpServlet
{
  private final ViewRegistry viewRegistry;

  private final ViewModuleBooter startup;

  @Inject
  public ComponentViewServlet(final ViewRegistry viewRegistry, final ViewModuleBooter startup) {
    this.viewRegistry = viewRegistry;
    this.startup = startup;
  }

  private boolean initialized = false;

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException
  {
    final PathDecomposer pathDecomposer = new PathDecomposer(req.getPathInfo());

    final View view = viewRegistry.getView(pathDecomposer.getViewName());
    if (view == null) {
      throw new ErrorStatusException(HttpServletResponse.SC_NOT_FOUND, "Not Found", "View not found.");
    }

    final ViewRequest request = new WrappingViewRequest(req, pathDecomposer.getViewName(), view.getConfig(),
        pathDecomposer.getRemainingPath());

    try {
      view.dispatch(request).send(resp);
    }
    catch (Exception e) {

      Throwables.propagate(e);
    }
  }
}
