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
package org.sonatype.nexus.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ChecksumReconciler;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.nexus.tasks.descriptors.ReconcileChecksumsTaskDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reconcile checksums task.
 *
 * @since 2.11.3
 */
@Named(ReconcileChecksumsTaskDescriptor.ID)
public class ReconcileChecksumsTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{
  /**
   * System event action: reconcileChecksums
   */
  public static final String ACTION = "RECONCILECHECKSUMS";

  private final ChecksumReconciler checksumReconciler;

  @Inject
  public ReconcileChecksumsTask(final ChecksumReconciler checksumReconciler) {
    this.checksumReconciler = checkNotNull(checksumReconciler);
  }

  @Override
  protected String getRepositoryFieldId() {
    return ReconcileChecksumsTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
  }

  @Override
  protected String getRepositoryPathFieldId() {
    return ReconcileChecksumsTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
  }

  @Override
  public Object doRun() throws Exception {
    final ResourceStoreRequest request = new ResourceStoreRequest(getResourceStorePath());

    if (getRepositoryId() != null) {
      final Repository repo = getRepositoryRegistry().getRepository(getRepositoryId());
      checksumReconciler.reconcileChecksums(repo, request);
    }
    else {
      for (final Repository repo : getRepositoryRegistry().getRepositories()) {
        checksumReconciler.reconcileChecksums(repo, request);
      }
    }

    return null;
  }

  @Override
  protected String getAction() {
    return ACTION;
  }

  @Override
  protected String getMessage() {
    String message;
    if (getRepositoryId() != null) {
      message = "Reconciling checksums of repository " + getRepositoryName();
    }
    else {
      message = "Reconciling checksums of all registered repositories";
    }
    return message + " from path " + getResourceStorePath() + " and below.";
  }

}
