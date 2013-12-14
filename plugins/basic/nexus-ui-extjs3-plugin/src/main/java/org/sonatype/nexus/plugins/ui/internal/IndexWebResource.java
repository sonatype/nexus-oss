package org.sonatype.nexus.plugins.ui.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor.UiContribution;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Extjs-3 UI {@code index.html} resource.
 *
 * @since 2.8
 */
@Named
@Singleton
public class IndexWebResource
  extends ComponentSupport
  implements WebResource
{
  private final Provider<HttpServletRequest> requestProvider;

  private final ApplicationStatusSource applicationStatusSource;

  private final TemplateEngine templateEngine;

  private final BuildNumberService buildNumberService;

  private final Set<UiContributor> uiContributors;

  @Inject
  public IndexWebResource(final Provider<HttpServletRequest> requestProvider,
                          final ApplicationStatusSource applicationStatusSource,
                          final TemplateEngine templateEngine,
                          final BuildNumberService buildNumberService,
                          final Set<UiContributor> uiContributors)
  {
    this.requestProvider = checkNotNull(requestProvider);
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.templateEngine = checkNotNull(templateEngine);
    this.buildNumberService = checkNotNull(buildNumberService);
    this.uiContributors = checkNotNull(uiContributors);
  }

  @Override
  public String getPath() {
    return "/index.html";
  }

  @Nullable
  @Override
  public String getContentType() {
    return "text/html";
  }

  @Override
  public long getSize() {
    return -1;
  }

  @Override
  public long getLastModified() {
    return System.currentTimeMillis();
  }

  @Override
  public boolean shouldCache() {
    return false;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return renderTemplate("index.vm");
  }

  private InputStream renderTemplate(final String templateName) throws IOException {
    SystemStatus systemStatus = applicationStatusSource.getSystemStatus();

    Map<String, Object> params = Maps.newHashMap();
    params.put("serviceBase", "service/local");
    params.put("contentBase", "content");
    params.put("nexusVersion", systemStatus.getVersion());
    params.put("nexusRoot", BaseUrlHolder.get());
    params.put("appName", systemStatus.getAppName());
    params.put("formattedAppName", systemStatus.getFormattedAppName());

    boolean debugMode = isDebugMode();
    params.put("debug", debugMode);

    List<UiContribution> contributions = Lists.newArrayList();
    for (UiContributor contributor : uiContributors) {
      UiContribution contribution = contributor.contribute(debugMode);
      if (contribution.isEnabled()) {
        contributions.add(contribution);
      }
    }
    params.put("rJsContributions", contributions);
    params.put("buildQualifier", buildNumberService.getBuildNumber());

    URL template = getClass().getResource("index.vm");
    checkState(template != null, "Missing template: %s", templateName);

    log.debug("Rendering template: {}", template);
    String content = templateEngine.render(this, template, params);

    return new ByteArrayInputStream(content.getBytes());
  }

  private boolean isDebugMode() {
    String query = requestProvider.get().getQueryString();
    return query != null && query.contains("debug");
  }
}
