package org.sonatype.nexus.views.rawbinaries.view;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.ViewResponse;
import org.sonatype.nexus.componentviews.responses.Responses;
import org.sonatype.nexus.componentviews.responses.StatusResponse;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinary;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import com.google.common.base.Throwables;
import com.google.common.html.HtmlEscapers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class RawBinariesHandler
    implements Handler
{
  private final RawBinaryStore binaryStore;

  public RawBinariesHandler(final RawBinaryStore binaryStore) {
    this.binaryStore = checkNotNull(binaryStore);
  }

  @Override
  public ViewResponse handle(final ViewRequest req) {
    switch (req.getMethod()) {

      case PUT:
        try {
          final boolean result = binaryStore.create(req.getPath(), req.getMimeType(), req.getInputStream());

          if (result) {
            return Responses.created();
          }
          else {
            return new StatusResponse(409,
                "Conflict: binary already exists. To replace it, DELETE it first.");
          }
        }
        catch (IOException e) {
          Throwables.propagate(e);
        }

      case DELETE:
        if (binaryStore.delete(req.getPath())) {
          return Responses.deleted();
        }
        else {
          return Responses.notFound("No artifact to be deleted at that location.");
        }

      case GET:
        final List<RawBinary> forPath = binaryStore.getForPath(req.getPath());

        // Do we have a single result whose path matches the request exactly?
        if (isSuccesfulRequestForSingleArtifact(req, forPath)) {
          final RawBinary binary = forPath.get(0);
          return Responses
              .streamResponse(binary.getInputStream(), binary.getMimeType(), binary.getModifiedDate().toDate());
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        // missing HTML encoding

        html.append("<h1>").append(HtmlEscapers.htmlEscaper().escape(req.getPath())).append("</h1>");
        html.append("<ul>");

        for (RawBinary binary : forPath) {
          html.append(new Formatter().format("<li><a href=\"$s\">$s</a></li>", binary.getPath(), binary.getPath()));
        }
        html.append("</ul>");
        html.append("</body></html>");

        return Responses.html(html.toString());
      default:
        return Responses.methodNotAllowed();
    }
  }

  private boolean isSuccesfulRequestForSingleArtifact(final ViewRequest req, final List<RawBinary> forPath) {
    return forPath.size() == 1 && forPath.get(0).getPath().equals(req.getPath());
  }
}
