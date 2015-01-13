/*
 * Copyright (c) 2007-2014 Sonatype, Inc. and Georgy Bolyuba. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.bolyuba.nexus.plugin.npm.task;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

import com.bolyuba.nexus.plugin.npm.service.internal.orient.OrientMetadataStore;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * NX Task that backs up NPM MetadataStore.
 *
 * @since 2.11
 */
@Named(NpmDbBackupTaskDescriptor.ID)
@Singleton
public class NpmDbBackupTask
    extends AbstractNexusRepositoriesTask<Void>
{
  private final OrientMetadataStore orientMetadataStore;

  @Inject
  public NpmDbBackupTask(final OrientMetadataStore orientMetadataStore) {
    this.orientMetadataStore = checkNotNull(orientMetadataStore);
  }

  @Override
  protected Void doRun() throws Exception {
    try {
      orientMetadataStore.backupDatabase();
    }
    catch (IOException e) {
      logger.warn("Error during npm DB backup", e);
    }
    return null;
  }

  @Override
  protected String getAction() {
    return "NPM-DB-BACKUP";
  }

  @Override
  protected String getMessage() {
    return "Backing up npm metadata database.";
  }
}
