/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.error.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractLastingConfigurable;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CErrorReporting;
import org.sonatype.nexus.configuration.model.CErrorReportingCoreConfiguration;
import org.sonatype.nexus.util.DigesterUtils;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.issue.IssueRetriever;
import org.sonatype.sisu.pr.ProjectManager;
import org.sonatype.sisu.pr.bundle.Archiver;
import org.sonatype.sisu.pr.bundle.Bundle;
import org.sonatype.sisu.pr.bundle.StorageManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.codehaus.plexus.swizzle.IssueSubmissionResult;
import org.codehaus.plexus.swizzle.IssueSubmitter;
import org.codehaus.plexus.swizzle.jira.authentication.AuthenticationSource;
import org.codehaus.plexus.swizzle.jira.authentication.DefaultAuthenticationSource;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Project;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Named
public class DefaultErrorReportingManager
    extends AbstractLastingConfigurable<CErrorReporting>
    implements ErrorReportingManager
{

  private final IssueSubmitter issueSubmitter;

  private final IssueRetriever issueRetriever;

  private final Archiver archiver;

  private final ProjectManager projectManager;

  private final StorageManager storageManager;

  private static final String DEFAULT_USERNAME = "sonatype_problem_reporting";

  @VisibleForTesting
  static final String ERROR_REPORT_DIR = "error-report-bundles";

  private Set<String> errorHashSet = Sets.newHashSet();

  // ==

  @Inject
  public DefaultErrorReportingManager(final EventBus eventBus,
                                      final ApplicationConfiguration applicationConfiguration,
                                      final Archiver archiver,
                                      final IssueRetriever issueRetriever,
                                      final IssueSubmitter issueSubmitter,
                                      final ProjectManager projectManager,
                                      final StorageManager storageManager)
  {
    super("Error Reporting", eventBus, applicationConfiguration);
    this.archiver = checkNotNull(archiver);
    this.issueRetriever = checkNotNull(issueRetriever);
    this.issueSubmitter = checkNotNull(issueSubmitter);
    this.projectManager = checkNotNull(projectManager);
    this.storageManager = checkNotNull(storageManager);
  }

  // ==

  @Override
  protected void initializeConfiguration()
      throws ConfigurationException
  {
    if (getApplicationConfiguration().getConfigurationModel() != null) {
      configure(getApplicationConfiguration());

      CErrorReporting config = getCurrentConfiguration(false);
      if (config != null) {
        issueSubmitter.setServerUrl(config.getJiraUrl());
        issueRetriever.setServerUrl(config.getJiraUrl());
      }

      AuthenticationSource credentials =
          new DefaultAuthenticationSource(getValidJIRAUsername(), getValidJIRAPassword());
      issueSubmitter.setCredentials(credentials);
      issueRetriever.setCredentials(credentials);
    }
  }

  @Override
  protected CoreConfiguration<CErrorReporting> wrapConfiguration(Object configuration)
      throws ConfigurationException
  {
    if (configuration instanceof ApplicationConfiguration) {
      return new CErrorReportingCoreConfiguration(getApplicationConfiguration());
    }
    else {
      throw new ConfigurationException("The passed configuration object is of class \""
          + configuration.getClass().getName() + "\" and not the required \""
          + ApplicationConfiguration.class.getName() + "\"!");
    }
  }

  // ==

  @Override
  public boolean isEnabled() {
    return getCurrentConfiguration(false).isEnabled();
  }

  @Override
  public void setEnabled(boolean value) {
    getCurrentConfiguration(true).setEnabled(value);
  }

  @Override
  public String getJIRAUrl() {
    return getCurrentConfiguration(false).getJiraUrl();
  }

  @Override
  public void setJIRAUrl(String url) {
    getCurrentConfiguration(true).setJiraUrl(url);
    issueSubmitter.setServerUrl(url);
    issueRetriever.setServerUrl(url);
  }

  @Override
  public String getJIRAUsername() {
    return getCurrentConfiguration(false).getJiraUsername();
  }

  protected String getValidJIRAUsername() {
    String username = getJIRAUsername();

    if (StringUtils.isEmpty(username)) {
      username = DEFAULT_USERNAME;
    }

    return username;
  }

  @Override
  public void setJIRAUsername(String username) {
    getCurrentConfiguration(true).setJiraUsername(username);
  }

  @Override
  public String getJIRAPassword() {
    return getCurrentConfiguration(false).getJiraPassword();
  }

  protected String getValidJIRAPassword() {
    String password = getJIRAPassword();

    if (StringUtils.isEmpty(password)) {
      password = DEFAULT_USERNAME;
    }

    return password;
  }

  @Override
  public void setJIRAPassword(String password) {
    getCurrentConfiguration(true).setJiraPassword(password);
  }

  @Override
  public String getJIRAProject() {
    return getCurrentConfiguration(false).getJiraProject();
  }

  @Override
  public void setJIRAProject(String pkey) {
    getCurrentConfiguration(true).setJiraProject(pkey);
  }

  // ==

  @Override
  public ErrorReportResponse handleError(ErrorReportRequest request)
      throws IssueSubmissionException
  {
    return handleError(request, getValidJIRAUsername(), getValidJIRAPassword());
  }

  @Override
  public ErrorReportResponse handleError(ErrorReportRequest request, String username, String password)
      throws IssueSubmissionException
  {
    Preconditions.checkState(username != null, "No username for error reporting given");
    Preconditions.checkState(password != null, "No password for error reporting given");

    return handleError(request, new DefaultAuthenticationSource(username, password));
  }

  public ErrorReportResponse handleError(final ErrorReportRequest request, final AuthenticationSource auth)
      throws IssueSubmissionException
  {
    ErrorReportResponse response = new ErrorReportResponse();

    try {
      if (request.isManual()) {
        getLogger().trace("Manual error report: '{}'", request.getTitle());
        IssueSubmissionRequest subRequest = buildRequest(request);

        submitIssue(auth, response, subRequest);
      }
      else if ((isEnabled() && shouldHandleReport(request)
          && !shouldIgnore(request.getThrowable()))) {
        getLogger().info("Detected Error in Nexus: {}. Generating a problem report...",
            getThrowableMessage(request.getThrowable()));
        IssueSubmissionRequest subRequest = buildRequest(request);

        List<Issue> existingIssues = retrieveIssues(subRequest.getSummary(), auth);

        if (existingIssues.isEmpty()) {
          submitIssue(auth, response, subRequest);
        }
        else {
          response.setJiraUrl(existingIssues.get(0).getLink());
          writeArchive(subRequest.getBundles(), existingIssues.get(0).getKey());
          getLogger().info(
              "Not reporting problem as it already exists in database: "
                  + existingIssues.iterator().next().getLink());
        }
      }
      else {
        if (getLogger().isInfoEnabled()) {
          String reason = "Nexus ignores this type of error";
          if (!isEnabled()) {
            reason = "reporting is not enabled";
          }
          else if (!shouldHandleReport(request)) {
            reason = "it has already being reported or it does not have an error message";
          }
          getLogger().info(
              "Detected Error in Nexus: {}. Skipping problem report generation because {}",
              getThrowableMessage(request.getThrowable()), reason
          );
        }
      }

      response.setSuccess(true);
    }
    catch (Exception e) {
      if (getLogger().isDebugEnabled()) {
        getLogger().warn("Error while submitting problem report: {}", e.getMessage(), e);
      }
      else {
        getLogger().warn("Error while submitting problem report: {}", e.getMessage());
      }

      Throwables.propagateIfInstanceOf(e, IssueSubmissionException.class);

      throw new IssueSubmissionException("Unable to submit problem report: " + e.getMessage(), e);
    }
    finally {
      storageManager.release();
    }

    return response;
  }

  private String getThrowableMessage(final Throwable throwable) {
    if (throwable != null && StringUtils.isNotEmpty(throwable.getMessage())) {
      return throwable.getMessage();
    }
    return "(no exception message available)";
  }

  private void submitIssue(final AuthenticationSource auth, final ErrorReportResponse response,
                           final IssueSubmissionRequest subRequest)
      throws IssueSubmissionException, IOException
  {
    try {
      IssueSubmissionResult result = issueSubmitter.submit(subRequest, auth);
      response.setCreated(true);
      response.setJiraUrl(result.getIssueUrl());
      writeArchive(result.getBundles(), result.getKey());
      getLogger().info("Problem report ticket " + result.getIssueUrl() + " was created.");
    }
    catch (IssueSubmissionException e) {
      writeArchive(subRequest.getBundles(), "NOTSUBMITTED");
      throw e;
    }
  }

  protected boolean shouldHandleReport(ErrorReportRequest request) {
    // if there is a title, we are talking about user generated, simply use it
    if (request.getTitle() != null) {
      return true;
    }

    if (request.getThrowable() != null && StringUtils.isNotEmpty(request.getThrowable().getMessage())) {
      String hash = DigesterUtils.getSha1Digest(request.getThrowable().getMessage());

      if (errorHashSet.contains(hash)) {
        getLogger().debug("Received an exception we already processed, ignoring.");
        return false;
      }
      else {
        errorHashSet.add(hash);
        return true;
      }
    }
    else {
      getLogger().debug("Received an empty message in exception, will not handle");
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  protected List<Issue> retrieveIssues(String description, AuthenticationSource auth) {
    try {
      issueRetriever.setCredentials(auth);
      Project project = issueRetriever.getProject(projectManager.getProject(null));
      List<Issue> issues = issueRetriever.getIssues("\"" + description + "\"", project);
      return issues;
    }
    catch (Exception e) {
      getLogger().error("Unable to query JIRA server to find if error report already exists", e);
      return Collections.emptyList();
    }

  }

  protected IssueSubmissionRequest buildRequest(ErrorReportRequest request) {
    IssueSubmissionRequest subRequest = new IssueSubmissionRequest();

    subRequest.setContext(request);
    subRequest.setError(request.getThrowable());

    subRequest.setProjectKey(getJIRAProject());

    if (request.isManual()) {
      subRequest.setSummary(request.getTitle());
      subRequest.setDescription(request.getDescription());
    }

    return subRequest;
  }

  @VisibleForTesting
  void writeArchive(Collection<Bundle> bundles, String suffix)
      throws IOException
  {
    if (bundles == null || bundles.isEmpty()) {
      getLogger().debug("No problem report bundle assembled");
      return;
    }

    Bundle bundle;
    if (!(bundles.size() == 1 && "application/zip".equals(
        (bundle = bundles.iterator().next()).getContentType()))) {
      bundle = archiver.createArchive(bundles);
    }

    File zipFile = getZipFile("nexus-error-bundle-" + suffix, "zip");
    getLogger().debug("Writing problem report bundle: '{}'", zipFile);

    OutputStream output = null;
    InputStream input = null;

    try {
      output = new FileOutputStream(zipFile);
      input = bundle.getInputStream();
      IOUtil.copy(input, output);
    }
    finally {
      IOUtil.close(input);
      IOUtil.close(output);
    }

    return;
  }

  @VisibleForTesting
  File getZipFile(String prefix, String suffix) {
    File zipDir = getApplicationConfiguration().getWorkingDirectory(ERROR_REPORT_DIR);

    if (!zipDir.exists()) {
      zipDir.mkdirs();
    }

    return new File(zipDir, prefix + "." + System.currentTimeMillis() + "." + suffix);
  }

  protected boolean shouldIgnore(Throwable throwable) {
    if (throwable != null) {
      if ("org.mortbay.jetty.EofException".equals(throwable.getClass().getName())
          || "org.eclipse.jetty.io.EofException".equals(throwable.getClass().getName())) {
        return true;
      }
      else if (throwable.getMessage() != null
          && (throwable.getMessage().contains("An exception occurred writing the response entity")
          || throwable.getMessage().contains("Error while handling an HTTP server call"))) {
        return true;
      }
    }

    return false;

  }

}
