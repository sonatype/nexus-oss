package org.sonatype.nexus.plugins.yum.rest.domain;

import java.net.URL;


public class UrlPathInterpretation {
  private final URL repoUrl;
  private final String path;
  private final boolean index;
  private final boolean redirect;
  private final String redirectUri;

  public UrlPathInterpretation(URL repoUrl, String path, boolean index) {
    this(repoUrl, path, index, false, null);
  }

  public UrlPathInterpretation(URL repoUrl, String path, boolean index, boolean redirect, String redirectUri) {
    this.repoUrl = repoUrl;
    this.path = path;
    this.index = index;
    this.redirect = redirect;
    this.redirectUri = redirectUri;
  }

  public String getPath() {
    return path;
  }

  public boolean isIndex() {
    return index;
  }

  public URL getRepositoryUrl() {
    return repoUrl;
  }

  public boolean isRedirect() {
    return redirect;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

}
