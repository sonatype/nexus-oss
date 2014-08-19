package org.sonatype.nexus.componentviews.internal;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.Request;
import org.sonatype.nexus.componentviews.Response;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.web.ErrorStatusException;

import com.google.common.base.Throwables;

/**
 * @since 3.0
 */
@Singleton
public class ComponentViewServlet
    extends HttpServlet
{
  private final ViewRegistry viewRegistry;

  @Inject
  public ComponentViewServlet(final ViewRegistry viewRegistry) {
    this.viewRegistry = viewRegistry;
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException
  {
    final Request request = createRequest(req);

    final View view = viewRegistry.getView(request.getViewName());
    if (view == null) {
      throw new ErrorStatusException(HttpServletResponse.SC_NOT_FOUND, "Not Found", "View not found.");
    }

    try {
      final Response response = view.dispatch(request);
      sendResponse(resp, response);
    }
    catch (Exception e) {

      Throwables.propagate(e);
    }

  }


  // BIG LIST OF RESPONSIBILITIES
  // Determining what routes there are to take
  // Managing the list of routes, and synchronizing all that when it changes on the fly (e.g. when the nuget feature is activated)
  // Assembling routes and the handler chains
  // Ordering the routes

  // Deciding if a given route matches a URI

  // initiate handling

  // Mapping bits of the request into more easily consumed java things
  // sdfdsf

  // Mapping responses in Java terms into the servlet response
  // e.g. JAX-RS's JAXB handling
  // writing out streams
  // returning HTML content

  // Could we have a 'sleeve' around some of the handlers, to allow JAX-RS style param/return code translation to
  // http servlet shit?
  // I think we need a common interface for handlers - they need to nest, don't they?  Or is it a sequence, that the
  // outer framework is handling? (Doesn't sound good, that's too global variable)

  // http response codes
  // cookies (god forbid)
  // handling exceptions and mapping those into response codes

  // Querying the component repository
  // Question: Do we see bits of static site content as components? I suppose we do.
  // How do we dispatch components that refer to other components, such as HTML referring to images? By preserving
  // relative URLs as things are deployed, I guess!
  // The 'raw/static' view would therefore need to use component metadata tags

  /**
   * Translate the {@link Response} into the {@link HttpServletResponse}.
   */
  private void sendResponse(final HttpServletResponse resp,
                            final Response response)
  {
    throw new UnsupportedOperationException("Not implemented.");
  }

  private Request createRequest(HttpServletRequest request) {
    request.getMethod(); // etc.
    throw new UnsupportedOperationException("Not implemented.");
  }

  private String firstPathSegment(String path) {
    final String[] split = path.split("/");
    if (split.length < 2) {
      return null;
    }
    return split[1];
  }
}
