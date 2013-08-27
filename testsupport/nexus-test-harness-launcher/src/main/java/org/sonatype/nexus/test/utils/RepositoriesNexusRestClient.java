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

import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.inError;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.isSuccess;
import static org.sonatype.nexus.test.utils.NexusRequestMatchers.isSuccessful;

public class RepositoriesNexusRestClient
{

  private static final Logger LOG = LoggerFactory.getLogger(RepositoriesNexusRestClient.class);

  public static final String ALL_SERVICE_PART = NexusRestClient.SERVICE_LOCAL + "all_repositories";

  public static final String SERVICE_PART = NexusRestClient.SERVICE_LOCAL + "repositories";

  private static final Integer USE_DEFAULT_ARTIFACT_MAX_AGE = null;

  private static final Integer USE_DEFAULT_METADATA_MAX_AGE = null;

  private static final RepositoryWritePolicy USE_DEFAULT_REPOSITORY_WRITE_POLICY = null;

  private final NexusRestClient nexusRestClient;

  private final TasksNexusRestClient taskNRC;

  private final EventInspectorsUtil eventNRC;

  private final XStream xstream;

  private final MediaType mediaType;

  public RepositoriesNexusRestClient(final NexusRestClient nexusRestClient,
                                     final TasksNexusRestClient taskNRC,
                                     final EventInspectorsUtil eventNRC)
  {
    this(nexusRestClient, taskNRC, eventNRC, XStreamFactory.getJsonXStream(), MediaType.APPLICATION_JSON);
  }

  public RepositoriesNexusRestClient(final NexusRestClient nexusRestClient,
                                     final TasksNexusRestClient taskNRC,
                                     final EventInspectorsUtil eventNRC,
                                     final XStream xstream,
                                     final MediaType mediaType)
  {
    this.nexusRestClient = checkNotNull(nexusRestClient);
    this.taskNRC = checkNotNull(taskNRC);
    this.eventNRC = checkNotNull(eventNRC);

    this.xstream = checkNotNull(xstream);
    this.mediaType = checkNotNull(mediaType);
  }

  public RepositoryBaseResource createRepository(final RepositoryBaseResource repo)
      throws IOException
  {
    Response response = null;
    RepositoryBaseResource responseResource;
    try {
      response = sendMessage(Method.POST, repo);
      assertThat(response, isSuccessful());
      responseResource = getRepositoryBaseResourceFromResponse(response);
    }
    finally {
      nexusRestClient.releaseResponse(response);
    }
    return responseResource;
  }

  public RepositoryBaseResource getRepository(String repoId)
      throws IOException
  {
    // accepted return codes: OK or redirect
    final String responseText = nexusRestClient.doGetForText(SERVICE_PART + "/" + repoId, not(inError()));
    LOG.debug("responseText: \n" + responseText);

    // this should use call to: getResourceFromResponse
    XStreamRepresentation representation =
        new XStreamRepresentation(XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML);

    RepositoryResourceResponse resourceResponse =
        (RepositoryResourceResponse) representation.getPayload(new RepositoryResourceResponse());

    return resourceResponse.getData();
  }

  public RepositoryBaseResource updateRepo(RepositoryBaseResource repo)
      throws IOException
  {

    Response response = null;
    RepositoryBaseResource responseResource;
    try {
      response = sendMessage(Method.PUT, repo);
      assertThat("Could not update user", response, isSuccessful());
      responseResource = getRepositoryBaseResourceFromResponse(response);
    }
    finally {
      nexusRestClient.releaseResponse(response);
    }

    return responseResource;
  }

  /**
   * Updates remote URL for a proxy repository.
   *
   * @param repositoryId proxy repository id
   * @param url          new URL
   * @return updated repository
   * @throws IOException if a problem occurred during update
   */
  public RepositoryBaseResource updateProxyRepositoryRemoteUrl(final String repositoryId, final String url)
      throws IOException
  {
    final RepositoryBaseResource repository = getRepository(repositoryId);
    assertThat(repository, instanceOf(RepositoryResource.class));
    ((RepositoryResource) repository).getRemoteStorage().setRemoteStorageUrl(url);
    return updateRepo(repository);
  }

  /**
   * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
   */
  public Response sendMessage(Method method, RepositoryBaseResource resource, String id)
      throws IOException
  {
    if (resource != null && resource.getProviderRole() == null) {
      if ("virtual".equals(resource.getRepoType())) {
        resource.setProviderRole(ShadowRepository.class.getName());
      }
      else {
        resource.setProviderRole(Repository.class.getName());
      }
    }

    XStreamRepresentation representation = new XStreamRepresentation(xstream, "", mediaType);

    String idPart = (method == Method.POST) ? "" : "/" + id;

    String serviceURI = SERVICE_PART + idPart;

    RepositoryResourceResponse repoResponseRequest = new RepositoryResourceResponse();
    repoResponseRequest.setData(resource);

    // now set the payload
    representation.setPayload(repoResponseRequest);

    LOG.debug("sendMessage: " + representation.getText());

    return nexusRestClient.sendMessage(serviceURI, method, representation);
  }

  /**
   * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
   */
  public Response sendMessage(Method method, RepositoryBaseResource resource)
      throws IOException
  {
    return sendMessage(method, resource, resource.getId());
  }

  /**
   * This should be replaced with a REST Call, but the REST client does not set the Accept correctly on GET's/
   */
  public List<RepositoryListResource> getList()
      throws IOException
  {
    String responseText = nexusRestClient.doGetForText(SERVICE_PART);
    LOG.debug("responseText: \n" + responseText);

    XStreamRepresentation representation =
        new XStreamRepresentation(XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML);

    RepositoryListResourceResponse resourceResponse =
        (RepositoryListResourceResponse) representation.getPayload(new RepositoryListResourceResponse());

    return resourceResponse.getData();

  }

  public List<RepositoryListResource> getAllList()
      throws IOException
  {
    String responseText = nexusRestClient.doGetForText(ALL_SERVICE_PART);
    LOG.debug("responseText: \n" + responseText);

    XStreamRepresentation representation =
        new XStreamRepresentation(XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML);

    RepositoryListResourceResponse resourceResponse =
        (RepositoryListResourceResponse) representation.getPayload(new RepositoryListResourceResponse());

    return resourceResponse.getData();
  }

  public RepositoryBaseResource getRepositoryBaseResourceFromResponse(Response response)
      throws IOException
  {
    String responseString = response.getEntity().getText();
    LOG.debug(" getRepositoryBaseResourceFromResponse: " + responseString);

    XStreamRepresentation representation = new XStreamRepresentation(xstream, responseString, mediaType);
    RepositoryResourceResponse resourceResponse =
        (RepositoryResourceResponse) representation.getPayload(new RepositoryResourceResponse());

    return resourceResponse.getData();
  }

  public RepositoryResource getResourceFromResponse(Response response)
      throws IOException
  {
    return (RepositoryResource) getRepositoryBaseResourceFromResponse(response);
  }

  public void updateIndexes(String... repositories)
      throws Exception
  {
    reindex(repositories, false);
  }

  private void reindex(String[] repositories, boolean incremental)
      throws IOException, Exception
  {
    for (String repo : repositories) {
      String serviceURI;
      if (incremental) {
        serviceURI = "service/local/data_incremental_index/repositories/" + repo + "/content";
      }
      else {
        serviceURI = "service/local/data_index/repositories/" + repo + "/content";
      }
      Status status = nexusRestClient.doDeleteForStatus(serviceURI, null);
      assertThat("Fail to update " + repo + " repository index " + status, status, isSuccess());
    }

    // let s w8 a few time for indexes
    taskNRC.waitForAllTasksToStop();
  }

  public void updateIncrementalIndexes(String... repositories)
      throws Exception
  {
    reindex(repositories, true);
  }

  public RepositoryStatusResource getStatus(String repoId)
      throws IOException
  {
    return getStatus(repoId, false);
  }

  public RepositoryStatusResource getStatus(String repoId, boolean force)
      throws IOException
  {

    String uri = SERVICE_PART + "/" + repoId + "/status";

    if (force) {
      uri = uri + "?forceCheck=true";
    }

    Response response = null;
    final String responseText;
    try {
      response = nexusRestClient.sendMessage(uri, Method.GET);
      responseText = response.getEntity().getText();
      assertThat("Fail to getStatus for '" + repoId + "' repository", response, isSuccessful());
    }
    finally {
      nexusRestClient.releaseResponse(response);
    }

    XStreamRepresentation representation =
        new XStreamRepresentation(xstream, responseText, MediaType.APPLICATION_XML);

    RepositoryStatusResourceResponse resourceResponse =
        (RepositoryStatusResourceResponse) representation.getPayload(new RepositoryStatusResourceResponse());

    return resourceResponse.getData();
  }

  public void updateStatus(RepositoryStatusResource repoStatus)
      throws IOException
  {
    String uriPart = SERVICE_PART + "/" + repoStatus.getId() + "/status";

    XStreamRepresentation representation = new XStreamRepresentation(xstream, "", MediaType.APPLICATION_XML);
    RepositoryStatusResourceResponse resourceResponse = new RepositoryStatusResourceResponse();
    resourceResponse.setData(repoStatus);
    representation.setPayload(resourceResponse);

    Response response = null;
    final String responseText;
    try {
      response = nexusRestClient.sendMessage(uriPart, Method.PUT, representation);
      responseText = response.getEntity().getText();
      assertThat("Fail to update '" + repoStatus.getId() + "' repository status " + response.getStatus()
          + "\nResponse:\n"
          + responseText + "\nrepresentation:\n" + representation.getText(), response,
          isSuccessful());
    }
    finally {
      nexusRestClient.releaseResponse(response);
    }
  }

  /**
   * @deprecated This is half baked stuff
   */
  public ContentListResourceResponse downloadRepoIndexContent(String repoId)
      throws IOException
  {
    String serviceURI = "service/local/repositories/" + repoId + "/index_content/";

    String responseText = nexusRestClient.doGetForText(serviceURI);

    XStreamRepresentation re =
        new XStreamRepresentation(XStreamFactory.getXmlXStream(), responseText, MediaType.APPLICATION_XML);
    ContentListResourceResponse resourceResponse =
        (ContentListResourceResponse) re.getPayload(new ContentListResourceResponse());

    return resourceResponse;
  }

  /**
   * Change block proxy state.<BR>
   * this method only return after all Tasks and Asynchronous events to finish
   */
  public void setBlockProxy(final String repoId, final boolean block)
      throws Exception
  {
    RepositoryStatusResource status = new RepositoryStatusResource();
    status.setId(repoId);
    status.setRepoType("proxy");
    status.setLocalStatus(LocalStatus.IN_SERVICE.name());
    if (block) {
      status.setRemoteStatus(RemoteStatus.AVAILABLE.name());
      status.setProxyMode(ProxyMode.BLOCKED_MANUAL.name());
    }
    else {
      status.setRemoteStatus(RemoteStatus.UNAVAILABLE.name());
      status.setProxyMode(ProxyMode.ALLOW.name());
    }
    Response response = changeStatus(status);

    try {
      assertThat("Could not unblock proxy: " + repoId + ", status: " + response.getStatus().getName() + " ("
          + response.getStatus().getCode() + ") - " + response.getStatus().getDescription(), response,
          isSuccessful());
    }
    finally {
      nexusRestClient.releaseResponse(response);
    }

    // wait for this action to be complete, since make no sense test if repo got block before blocking was really
    // enforced
    taskNRC.waitForAllTasksToStop();
    eventNRC.waitForCalmPeriod();
  }

  /**
   * Change block out of service state.<BR>
   * this method only return after all Tasks and Asynchronous events to finish
   */
  public void setOutOfServiceProxy(final String repoId, final boolean outOfService)
      throws Exception
  {

    RepositoryStatusResource status = new RepositoryStatusResource();
    status.setId(repoId);
    status.setRepoType("proxy");
    if (outOfService) {
      status.setLocalStatus(LocalStatus.OUT_OF_SERVICE.name());
    }
    else {
      status.setLocalStatus(LocalStatus.IN_SERVICE.name());
    }
    Response response = changeStatus(status);
    try {
      assertThat("Could not set proxy out of service status (Status: " + response.getStatus() + ": " + repoId
          + "\n" + response.getEntity().getText(), response, isSuccessful());
    }
    finally {
      nexusRestClient.releaseResponse(response);
    }

    // wait for this action to be complete, since make no sense test if repo got out of service before oos was
    // really enforced
    taskNRC.waitForAllTasksToStop();
    eventNRC.waitForCalmPeriod();
  }

  public Response putOutOfService(String repoId, String repoType)
      throws IOException
  {
    RepositoryStatusResource status = new RepositoryStatusResource();
    status.setId(repoId);
    status.setRepoType(repoType);
    status.setLocalStatus(LocalStatus.OUT_OF_SERVICE.name());
    return changeStatus(status);
  }

  public Response putInService(String repoId, String repoType)
      throws IOException
  {
    RepositoryStatusResource status = new RepositoryStatusResource();
    status.setId(repoId);
    status.setRepoType(repoType);
    status.setLocalStatus(LocalStatus.IN_SERVICE.name());
    return changeStatus(status);
  }

  /**
   * IMPORTANT: Make sure to release the Response in a finally block when you are done with it.
   */
  public Response changeStatus(RepositoryStatusResource status)
      throws IOException
  {
    String serviceURI = "service/local/repositories/" + status.getId() + "/status?undefined";

    XStreamRepresentation representation =
        new XStreamRepresentation(XStreamFactory.getXmlXStream(), "", MediaType.APPLICATION_XML);
    RepositoryStatusResourceResponse request = new RepositoryStatusResourceResponse();
    request.setData(status);
    representation.setPayload(request);

    Response response = nexusRestClient.sendMessage(serviceURI, Method.PUT, representation);
    return response;
  }

  public void createMavenHostedReleaseRepository(final String id)
      throws IOException
  {
    createMavenHostedRepository(id, RepositoryPolicy.RELEASE);
  }

  public void createMavenHostedSnapshotRepository(final String id)
      throws IOException
  {
    createMavenHostedRepository(id, RepositoryPolicy.SNAPSHOT);
  }

  public void createMavenHostedRepository(final String id, final RepositoryPolicy repositoryPolicy)
      throws IOException
  {
    createMavenHostedRepository(id, repositoryPolicy, USE_DEFAULT_REPOSITORY_WRITE_POLICY);
  }

  public void createMavenHostedRepository(final String id,
                                          final RepositoryPolicy repositoryPolicy,
                                          final RepositoryWritePolicy repositoryWritePolicy)
      throws IOException
  {
    final RepositoryResource repository = new RepositoryResource();

    repository.setId(id);
    repository.setRepoType("hosted");
    repository.setName(id);
    repository.setProvider("maven2");
    repository.setFormat("maven2");
    repository.setRepoPolicy(repositoryPolicy.name());
    if (repositoryWritePolicy != null) {
      repository.setWritePolicy(repositoryWritePolicy.name());
    }
    repository.setChecksumPolicy(ChecksumPolicy.IGNORE.name());
    repository.setBrowseable(true);
    repository.setIndexable(true);
    repository.setExposed(true);

    createRepository(repository);
  }

  public void createMavenProxyReleaseRepository(final String id, final String url)
      throws IOException
  {
    createMavenProxyReleaseRepository(id, url, USE_DEFAULT_ARTIFACT_MAX_AGE, USE_DEFAULT_METADATA_MAX_AGE);
  }

  public void createMavenProxyReleaseRepository(final String id,
                                                final String url,
                                                final Integer artifactMaxAge,
                                                final Integer metadataMaxAge)
      throws IOException
  {
    createMavenProxyRepository(id, url, RepositoryPolicy.RELEASE, artifactMaxAge, metadataMaxAge);
  }

  public void createMavenProxySnapshotRepository(final String id, final String url)
      throws IOException
  {
    createMavenProxySnapshotRepository(id, url, USE_DEFAULT_ARTIFACT_MAX_AGE, USE_DEFAULT_METADATA_MAX_AGE);
  }

  public void createMavenProxySnapshotRepository(final String id,
                                                 final String url,
                                                 final Integer artifactMaxAge,
                                                 final Integer metadataMaxAge)
      throws IOException
  {
    createMavenProxyRepository(id, url, RepositoryPolicy.SNAPSHOT, artifactMaxAge, metadataMaxAge);
  }

  public void createMavenProxyRepository(final String id,
                                         final String url,
                                         final RepositoryPolicy repositoryPolicy,
                                         final Integer artifactMaxAge,
                                         final Integer metadataMaxAge)
      throws IOException
  {
    final RepositoryProxyResource repository = new RepositoryProxyResource();

    repository.setId(id);
    repository.setRepoType("proxy");
    repository.setName(id);
    repository.setProvider("maven2");
    repository.setFormat("maven2");
    repository.setRepoPolicy(repositoryPolicy.name());
    repository.setWritePolicy(RepositoryWritePolicy.READ_ONLY.name());
    repository.setDownloadRemoteIndexes(true);
    repository.setChecksumPolicy(ChecksumPolicy.IGNORE.name());
    repository.setBrowseable(true);
    repository.setIndexable(true);
    repository.setExposed(true);
    if (artifactMaxAge != null) {
      repository.setArtifactMaxAge(artifactMaxAge);
    }
    if (metadataMaxAge != null) {
      repository.setMetadataMaxAge(metadataMaxAge);
    }

    RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
    remoteStorage.setRemoteStorageUrl(url);
    repository.setRemoteStorage(remoteStorage);

    createRepository(repository);
  }

}
