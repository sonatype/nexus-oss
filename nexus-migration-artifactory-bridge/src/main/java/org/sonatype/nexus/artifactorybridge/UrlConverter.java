package org.sonatype.nexus.artifactorybridge;

public interface UrlConverter
{

    String convertDownload(String servletPath);

    String convertDeploy(String servletPath);

}
