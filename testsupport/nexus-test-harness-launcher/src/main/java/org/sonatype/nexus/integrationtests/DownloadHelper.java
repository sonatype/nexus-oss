/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.sonatype.nexus.test.utils.TestProperties;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.test.utils.GavUtil.getRelitiveArtifactPath;
import static org.sonatype.nexus.test.utils.ResponseMatchers.isRedirecting;
import static org.sonatype.nexus.test.utils.ResponseMatchers.redirectLocation;
import static org.sonatype.nexus.test.utils.ResponseMatchers.respondsWithStatusCode;

/**
 * Helpers to download content from repositories.
 *
 * @since 3.0
 */
public class DownloadHelper
{
  private static final Logger log = LoggerFactory.getLogger(DownloadHelper.class);

  private static final String REPOSITORY_RELATIVE_URL = "content/repositories/";

  private static final String GROUP_REPOSITORY_RELATIVE_URL = "content/groups/";

  private String getBaseNexusUrl() {
    return TestProperties.getString("nexus.base.url");
  }

  public File downloadSnapshotArtifact(String repository, Gav gav, File parentDir) throws IOException {
    // @see http://issues.sonatype.org/browse/NEXUS-599
    // r=<repoId> -- mandatory
    // g=<groupId> -- mandatory
    // a=<artifactId> -- mandatory
    // v=<version> -- mandatory
    // c=<classifier> -- optional
    // p=<packaging> -- optional, jar is taken as default
    // http://localhost:8087/nexus/service/local/artifact/maven/redirect?r=tasks-snapshot-repo&g=nexus&a=artifact&v=1.0-SNAPSHOT

    String c = gav.getClassifier() == null ? "" : "&c=" + Reference.encode(gav.getClassifier());
    String serviceURI = String.format("service/local/artifact/maven/redirect?r=%s&g=%s&a=%s&v=%s%s",
        repository, gav.getGroupId(), gav.getArtifactId(), Reference.encode(gav.getVersion()), c);

    Response response = null;
    try {
      response = RequestFacade.doGetRequest(serviceURI);

      assertThat(response, allOf(
              isRedirecting(),
              respondsWithStatusCode(307),
              redirectLocation(notNullValue(String.class)))
      );

      serviceURI = response.getLocationRef().toString();
    }
    finally {
      RequestFacade.releaseResponse(response);
    }

    parentDir.mkdirs();
    File file = File.createTempFile(gav.getArtifactId(), '.' + gav.getExtension(), parentDir);
    RequestFacade.downloadFile(new URL(serviceURI), file.getAbsolutePath());

    return file;
  }

  public Metadata downloadMetadataFromRepository(Gav gav, String repoId)
      throws IOException, XmlPullParserException
  {
    String url = String.format("%s%s%s/%s/%s/maven-metadata.xml",
        getBaseNexusUrl(),
        REPOSITORY_RELATIVE_URL,
        repoId,
        gav.getGroupId(),
        gav.getArtifactId());

    Response response = null;
    try {
      response = RequestFacade.sendMessage(new URL(url), Method.GET, null);
      if (response.getStatus().isError()) {
        return null;
      }
      try (InputStream stream = response.getEntity().getStream()) {
        MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
        return metadataReader.read(stream);
      }
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  public File downloadArtifact(String baseUrl, String groupId, String artifact, String version, String type, String classifier, String targetDirectory)
      throws IOException
  {
    URL url = new URL(baseUrl + getRelitiveArtifactPath(groupId, artifact, version, type, classifier));

    String classifierPart = (classifier != null) ? "-" + classifier : "";
    return RequestFacade.downloadFile(url,
        String.format("%s/%s-%s%s.%s", targetDirectory, artifact, version, classifierPart, type));
  }

  public File downloadArtifact(String baseUrl, Gav gav, String targetDirectory) throws IOException {
    return downloadArtifact(
        baseUrl,
        gav.getGroupId(),
        gav.getArtifactId(),
        gav.getVersion(),
        gav.getExtension(),
        gav.getClassifier(),
        targetDirectory
    );
  }

  public File downloadArtifactFromRepository(String repoId, Gav gav, String targetDirectory) throws IOException {
    return downloadArtifact(String.format("%s%s%s/", getBaseNexusUrl(), REPOSITORY_RELATIVE_URL, repoId),
        gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(), gav.getClassifier(),
        targetDirectory);
  }

  public File downloadArtifactFromGroup(String groupId, Gav gav, String targetDirectory) throws IOException {
    return downloadArtifact(String.format("%s%s%s/", getBaseNexusUrl(), GROUP_REPOSITORY_RELATIVE_URL, groupId),
        gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
        gav.getClassifier(), targetDirectory);
  }
}
