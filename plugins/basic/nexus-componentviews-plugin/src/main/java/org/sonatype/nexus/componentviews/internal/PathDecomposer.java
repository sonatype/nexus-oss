package org.sonatype.nexus.componentviews.internal;

/**
 * A utility class for parsing the view name out of a request URI.
 *
 * @since 3.0
 */
public class PathDecomposer
{
  private final String path;

  private final String viewName;

  private final String remainingPath;

  public PathDecomposer(final String path) {
    this.path = path == null || "".equals(path) ? "/" : path;
    viewName = firstPathSegment(this.path);
    remainingPath = remainingPath(this.path);
  }

  public String getViewName() {
    return viewName;
  }

  public String getRemainingPath() {
    return remainingPath;
  }

  private String firstPathSegment(String path) {
    final String[] split = path.split("/");
    if (split.length < 2) {
      return "";
    }
    return split[1];
  }

  private String remainingPath(final String wholePath) {
    final String substring = wholePath.substring(viewName.length() + 1);

    if (substring.startsWith("/")) {
      return substring;
    }

    return "/" + substring;
  }
}
