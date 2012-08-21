package org.sonatype.nexus.plugins.yum.rest;

import static org.sonatype.nexus.plugins.yum.repository.YumRepository.YUM_REPOSITORY_DIR_NAME;
import static org.apache.commons.lang.StringUtils.join;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;

import org.sonatype.nexus.plugins.yum.rest.domain.UrlPathInterpretation;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.plugins.yum.rest.domain.UrlPathInterpretation;


public class UrlPathParser {
  private static final int FIRST_PARAM = 0;
  private static final int SECOND_PARAM = 1;
  private final String segmentPrefix;
  private final int segmentsAfterPrefix;
  private final String yumBaseUrl;

  public UrlPathParser(String yumBaseUrl, String yumRepoPrefixName, int segmentsAfterPrefix) {
    this.yumBaseUrl = yumBaseUrl;
    this.segmentPrefix = yumRepoPrefixName;
    this.segmentsAfterPrefix = segmentsAfterPrefix;
  }

  public UrlPathInterpretation interprete(Request request) throws ResourceException {
    List<String> segments = request.getResourceRef().getSegments();

    int yumIndex = segments.indexOf(segmentPrefix);
    if (yumIndex < 0) {
      throw new ResourceException(SERVER_ERROR_INTERNAL, "Prefix '" + segmentPrefix + "' not found.");
    }

    URL repoUrl;
    try {
      repoUrl = new URL(yumBaseUrl + "/" + join(segments.subList(yumIndex, yumIndex + segmentsAfterPrefix + 1), "/"));
    } catch (MalformedURLException e) {
      throw new ResourceException(SERVER_ERROR_INTERNAL, e);
    }

    List<String> lastSegments = segments.subList(yumIndex + segmentsAfterPrefix + 1, segments.size());

    if (lastSegments.contains("..")) {
      throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Requests with '..' are not allowed");
    }

    if (lastSegments.isEmpty()) {
      return new UrlPathInterpretation(repoUrl, null, true, true, pathToIndex(segments));
    }

    if (lastSegments.get(FIRST_PARAM).length() == 0) {
      return new UrlPathInterpretation(repoUrl, null, true);
    }

    if (YumRepository.YUM_REPOSITORY_DIR_NAME.equals(lastSegments.get(FIRST_PARAM))) {
      if (lastSegments.size() == 1) {
        return new UrlPathInterpretation(repoUrl, YumRepository.YUM_REPOSITORY_DIR_NAME, true, true, pathToIndex(segments));
      }

      if (lastSegments.get(SECOND_PARAM).length() == 0) {
        return new UrlPathInterpretation(repoUrl, YumRepository.YUM_REPOSITORY_DIR_NAME, true);
      }

      if (lastSegments.size() == 2) {
        return new UrlPathInterpretation(repoUrl, lastSegments.get(FIRST_PARAM) + "/" + lastSegments.get(SECOND_PARAM),
          false);
      }
    }

    return new UrlPathInterpretation(repoUrl, join(lastSegments, "/"), false, false, null);
  }

  private String pathToIndex(List<String> segments) {
    return "/" + join(segments, "/") + "/";
  }

}
