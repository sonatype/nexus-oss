/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.StringUtils;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

/**
 * Am ArtifactStore helper class, that simply drives a MavenRepository and gets various infos from it. It uses the
 * Repository interface of it's "owner" repository for storing/retrieval.
 *
 * @author cstamas
 */
public class ArtifactStoreHelper
{
  private final MavenRepository repository;

  protected ArtifactStoreHelper(MavenRepository repo) {
    super();

    this.repository = repo;
  }

  public MavenRepository getMavenRepository() {
    return repository;
  }

  public StorageFileItem retrieveArtifactPom(ArtifactStoreRequest gavRequest)
      throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
  {
    Gav pomGav =
        new Gav(
            gavRequest.getGav().getGroupId(),
            gavRequest.getGav().getArtifactId(),
            gavRequest.getGav().getVersion(),
            null, // gavRequest.getGav().getClassifier(),
            "pom", // gavRequest.getGav().getExtension(),
            gavRequest.getGav().getSnapshotBuildNumber(), gavRequest.getGav().getSnapshotTimeStamp(),
            gavRequest.getGav().getName(), gavRequest.getGav().isHash(), gavRequest.getGav().getHashType(),
            gavRequest.getGav().isSignature(), gavRequest.getGav().getSignatureType());

    ArtifactStoreRequest pomRequest =
        new ArtifactStoreRequest(gavRequest.getMavenRepository(), pomGav, gavRequest.isRequestLocalOnly(),
            gavRequest.isRequestRemoteOnly());

    return retrieveArtifact(pomRequest);
  }

  public Gav resolveArtifact(ArtifactStoreRequest gavRequest)
      throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
  {
    checkRequest(gavRequest);

    try {
      Gav gav = repository.getMetadataManager().resolveArtifact(gavRequest);

      if (gav == null) {
        throw new ItemNotFoundException(reasonFor(gavRequest, repository,
            "Request %s is not resolvable in repository %s", gavRequest.getRequestPath(),
            RepositoryStringUtils.getHumanizedNameString(repository)));
      }

      return gav;
    }
    catch (IOException e) {
      throw new LocalStorageException("Could not maintain metadata!", e);
    }
  }

  public StorageFileItem retrieveArtifact(ArtifactStoreRequest gavRequest)
      throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
  {
    checkRequest(gavRequest);

    Gav gav = resolveArtifact(gavRequest);

    gavRequest.setRequestPath(repository.getGavCalculator().gavToPath(gav));

    StorageItem item = repository.retrieveItem(gavRequest);

    if (StorageFileItem.class.isAssignableFrom(item.getClass())) {
      return (StorageFileItem) item;
    }
    else {
      throw new LocalStorageException("The Artifact retrieval returned non-file, path:"
          + item.getRepositoryItemUid().toString());
    }
  }

  public void storeArtifactPom(ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> attributes)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
             StorageException, AccessDeniedException
  {
    checkRequest(gavRequest);

    Gav gav =
        new Gav(gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(),
            gavRequest.getClassifier(), "pom", null, null, null, false, null, false, null);

    gavRequest.setRequestPath(repository.getGavCalculator().gavToPath(gav));

    repository.storeItem(gavRequest, is, attributes);

    try {
      repository.getMetadataManager().deployArtifact(gavRequest);
    }
    catch (IOException e) {
      throw new LocalStorageException("Could not maintain metadata!", e);
    }
  }

  public void storeArtifact(ArtifactStoreRequest gavRequest, InputStream is, Map<String, String> attributes)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
             StorageException, AccessDeniedException
  {
    checkRequest(gavRequest);

    Gav gav =
        new Gav(gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(),
            gavRequest.getClassifier(), gavRequest.getExtension(), null, null, null, false, null, false, null);

    gavRequest.setRequestPath(repository.getGavCalculator().gavToPath(gav));

    repository.storeItem(gavRequest, is, attributes);
  }

  public void storeArtifactWithGeneratedPom(ArtifactStoreRequest gavRequest, String packaging, InputStream is,
                                            Map<String, String> attributes)
      throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
             StorageException, AccessDeniedException
  {
    checkRequest(gavRequest);

    // Force classifier to null, as the pom shouldn't have a classifier
    final Gav pomGav =
        new Gav(gavRequest.getGroupId(), gavRequest.getArtifactId(), gavRequest.getVersion(), null, "pom", null,
            null, null, false, null, false, null);
    final ArtifactStoreRequest pomRequest = new ArtifactStoreRequest(gavRequest.getMavenRepository(), pomGav, false);
    pomRequest.getRequestContext().setParentContext(gavRequest.getRequestContext());

    try {
      // check for POM existence
      repository.retrieveItem(false, pomRequest);
    }
    catch (ItemNotFoundException e) {
      if (StringUtils.isBlank(packaging)) {
        throw new IllegalArgumentException("Cannot generate POM without valid 'packaging'!");
      }

      // POM does not exists
      // generate minimal POM
      // got from install:install-file plugin/mojo, thanks
      Model model = new Model();
      model.setModelVersion("4.0.0");
      model.setGroupId(gavRequest.getGroupId());
      model.setArtifactId(gavRequest.getArtifactId());
      model.setVersion(gavRequest.getVersion());
      model.setPackaging(packaging);
      model.setDescription("POM was created by Sonatype Nexus");

      StringWriter sw = new StringWriter();

      MavenXpp3Writer mw = new MavenXpp3Writer();

      try {
        mw.write(sw, model);
      }
      catch (IOException ex) {
        // writing to string, not to happen
      }

      repository.storeItem(pomRequest, new ByteArrayInputStream(sw.toString().getBytes()), attributes);

      try {
        repository.getMetadataManager().deployArtifact(pomRequest);
      }
      catch (IOException ex) {
        throw new LocalStorageException("Could not maintain metadata!", ex);
      }

    }

    // reset path if anything changed it
    gavRequest.setRequestPath(repository.getGavCalculator().gavToPath(gavRequest.getGav()));
    repository.storeItem(gavRequest, is, attributes);
  }

  // =======================================================================================

  protected void checkRequest(ArtifactStoreRequest gavRequest) {
    if (gavRequest.getGroupId() == null || gavRequest.getArtifactId() == null || gavRequest.getVersion() == null) {
      throw new IllegalArgumentException("GAV is not supplied or only partially supplied! (G: '"
          + gavRequest.getGroupId() + "', A: '" + gavRequest.getArtifactId() + "', V: '"
          + gavRequest.getVersion() + "')");
    }
  }

}
