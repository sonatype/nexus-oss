package org.sonatype.nexus.plugins.yum.rest.domain;

import org.restlet.data.MediaType;
import org.restlet.resource.FileRepresentation;
import org.sonatype.nexus.plugins.yum.repository.FileDirectoryStructure;
import org.sonatype.nexus.plugins.yum.repository.FileDirectoryStructure;


public class YumFileRepresentation extends FileRepresentation {
  public YumFileRepresentation(UrlPathInterpretation interpretation, FileDirectoryStructure fileDirectoryStructure) {
    super(fileDirectoryStructure.getFile(interpretation.getPath()), getMediaType(interpretation.getPath()));
  }

  private static MediaType getMediaType(String path) {
    if (path.endsWith("xml")) {
      return MediaType.APPLICATION_XML;
    } else if (path.endsWith("gz")) {
      return MediaType.APPLICATION_GNU_ZIP;
    } else {
      return MediaType.APPLICATION_ALL;
    }
  }
}
