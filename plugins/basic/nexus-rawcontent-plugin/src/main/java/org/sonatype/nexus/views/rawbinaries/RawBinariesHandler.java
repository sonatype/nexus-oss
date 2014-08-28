package org.sonatype.nexus.views.rawbinaries;

import java.util.Formatter;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.Request;
import org.sonatype.nexus.componentviews.responses.Response;
import org.sonatype.nexus.componentviews.responses.Responses;
import org.sonatype.nexus.componentviews.responses.StatusResponse;

import com.google.common.html.HtmlEscapers;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named
@Singleton
public class RawBinariesHandler
    implements Handler
{
  private final RawBinaryStore binaryStore;

  private final BlobStore blobStore;

  public RawBinariesHandler(@Named("rawBinary") final BlobStore blobStore, final RawBinaryStore binaryStore) {
    this.binaryStore = checkNotNull(binaryStore);
    this.blobStore = checkNotNull(blobStore);
  }

  @Override
  public Response handle(final Request req) {
    switch (req.getMethod()) {

      case PUT:
        final boolean result = binaryStore.create(req.getPath(), req.getMimeType(), req.getInputStream());
        if (result) {
          return Responses.created();
        }
        else {
          return new StatusResponse(409,
              "Conflict: binary already exists. To replace it, DELETE it first.");
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

  private boolean isSuccesfulRequestForSingleArtifact(final Request req, final List<RawBinary> forPath) {
    return forPath.size() == 1 && forPath.get(0).getPath().equals(req.getPath());
  }
}
