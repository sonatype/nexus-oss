package org.sonatype.nexus.componentviews.example;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.Request;
import org.sonatype.nexus.componentviews.Response;

/**
 * This example handler is stateful because it uses in-memory persistence; in practice this would be rare or
 * prohibited.
 *
 * @since 3.0
 */
public class ExamplePathListHandler
    implements Handler
{
  private final Map<String, Date> entries = new ConcurrentHashMap<>();

  @Override
  public Response handle(final Request req) {
    final String requestPath = req.getPath();

    switch (req.getMethod()) {
      case GET:
        SortedSet<String> paths = new TreeSet<String>();
        for (String path : entries.keySet()) {
          if (path.startsWith(requestPath)) {
            paths.add(path);
          }
        }

        final StringBuilder s = new StringBuilder();
        s.append("<html><body>");
        s.append("<ul>");
        for (String path : paths) {
          s.append("<li>");
          s.append(path).append(" ").append(new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z").format(
              entries.get(path)));
          s.append("</li>");
        }
        s.append("</ul>");

        s.append("</body></html>");

        // TODO: Okay, this sucks. Be nice to have a convenience API that knows how to do smarter things with content.
        // That way I could just return an input stream, a String.
        return Response.content("text/html", toInputStream(s.toString())).status(200).build();

      case PUT:
        entries.put(requestPath, new Date());
        return Response.status(201).build();

      case DELETE:
        final Date remove = entries.remove(requestPath);
        if (remove != null) {
          return Response.status(204).build();
        }
        else {
          return Response.status(404).build();
        }

      default:
        throw new IllegalArgumentException("This should never happen.");
    }
  }

  private ByteArrayInputStream toInputStream(
      final String string)
  {
    return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
  }
}
