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

package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ErrorReportRequest;
import org.sonatype.nexus.rest.model.ErrorReportRequestDTO;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;

import com.thoughtworks.xstream.XStream;
import org.codehaus.plexus.util.FileUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.restlet.data.MediaType;

import static org.hamcrest.Matchers.allOf;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.isSuccessful;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.respondsWithStatusCode;

public class ErrorReportUtil
{
  private static XStream xstream = XStreamFactory.getXmlXStream();

  public static ErrorReportResponse generateProblemReport(String title, String description)
      throws IOException
  {
    return generateProblemReport(title, description, null, null);
  }

  public static ErrorReportResponse generateProblemReport(String title, String description, String jiraUser,
                                                          String jiraPassword)
      throws IOException
  {
    if (title != null) {
      final String text = matchProblemResponse(title, description, jiraUser, jiraPassword, isSuccessful());

      XStreamRepresentation representation =
          new XStreamRepresentation(xstream, text, MediaType.APPLICATION_XML);

      ErrorReportResponse responseObj =
          (ErrorReportResponse) representation.getPayload(new ErrorReportResponse());

      return responseObj;
    }
    else {
      matchProblemResponse(title, description, jiraUser, jiraPassword, respondsWithStatusCode(400));
    }
    return null;
  }

  public static String matchProblemResponse(String title, String description, String jiraUser,
                                            String jiraPassword, Matcher... matchers)
      throws IOException
  {
    return RequestFacade.doPutForText("service/local/error_reporting",
        createErrorReportRequest(title, description, jiraUser, jiraPassword),
        allOf(matchers));
  }

  private static XStreamRepresentation createErrorReportRequest(final String title, final String description,
                                                                final String jiraUser,
                                                                final String jiraPassword)
  {
    ErrorReportRequest request = new ErrorReportRequest();
    request.setData(new ErrorReportRequestDTO());
    request.getData().setTitle(title);
    request.getData().setDescription(description);
    if (jiraUser != null) {
      request.getData().setErrorReportingSettings(new ErrorReportingSettings());
      request.getData().getErrorReportingSettings().setJiraUsername(jiraUser);
      request.getData().getErrorReportingSettings().setJiraPassword(jiraPassword);
    }

    XStreamRepresentation representation = new XStreamRepresentation(xstream, "", MediaType.APPLICATION_XML);
    representation.setPayload(request);
    return representation;
  }

  public static void cleanErrorBundleDir(String directory)
      throws IOException
  {
    File errorBundleDir = new File(directory + "/error-report-bundles");

    if (errorBundleDir.exists()) {
      FileUtils.deleteDirectory(errorBundleDir);
    }
  }

  public static void validateNoZip(String directory) {
    File errorBundleDir = new File(directory + "/error-report-bundles");

    Assert.assertFalse(errorBundleDir.exists());
  }

  public static void validateZipContents(String directory)
      throws IOException
  {
    File errorBundleDir = new File(directory + "/error-report-bundles");

    Assert.assertTrue(errorBundleDir.exists());

    File[] files = errorBundleDir.listFiles();

    Assert.assertNotNull(files);
    Assert.assertEquals(files.length, 1);
    Assert.assertTrue(files[0].getName().startsWith("nexus-error-bundle"));
    Assert.assertTrue(files[0].getName().endsWith(".zip"));

    validateZipContents(files[0]);
  }

  public static void validateZipContents(File file)
      throws IOException
  {
    ZipFile zipFile = new ZipFile(file);

    try {
      MatcherAssert.assertThat(zipFile, Matchers.allOf(
          FileMatchers.containsEntry("exception.txt"),
          FileMatchers.containsEntry("nexus.xml"),
          FileMatchers.containsEntry("security.xml"),
          FileMatchers.containsEntry("security-configuration.xml"),
          FileMatchers.containsEntry("contextListing.txt"),
          FileMatchers.containsEntry("conf/logback.properties"),
          FileMatchers.containsEntry("conf/logback-nexus.xml"),
          FileMatchers.containsEntry("conf/logback.xml")
      ));
    }
    finally {
      zipFile.close();
    }
  }
}
