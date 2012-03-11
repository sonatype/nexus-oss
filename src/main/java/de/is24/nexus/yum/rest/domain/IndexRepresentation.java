package de.is24.nexus.yum.rest.domain;

import java.io.File;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;
import de.is24.nexus.yum.repository.FileDirectoryStructure;


public class IndexRepresentation extends StringRepresentation {
  public IndexRepresentation(UrlPathInterpretation interpretation, FileDirectoryStructure fileDirectoryStructure) {
    super(generateRepoIndex(fileDirectoryStructure, interpretation));
    setMediaType(MediaType.TEXT_HTML);
  }

  private static CharSequence generateRepoIndex(FileDirectoryStructure fileDirectoryStructure,
    UrlPathInterpretation interpretation) {
    StringBuilder builder = new StringBuilder();
    builder.append("<html><head><title>File list</title></head><body><ul>");

    File directory = fileDirectoryStructure.getFile(interpretation.getPath());

    appendFiles(builder, directory.listFiles());

    builder.append("</ul></html>");
    return builder.toString();
  }

  private static void appendFiles(StringBuilder builder, File[] files) {
    for (File file : files) {
      String name = file.getName();
      if (file.isDirectory()) {
        name += "/";
      }
      builder.append(String.format("<li><a href=\"%s\">%s</a></li>", name, name));
    }
  }
}
