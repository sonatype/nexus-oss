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

package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import org.codehaus.plexus.util.FileUtils;

/**
 * AttributeStorage implementation that uses it's own FS storage to store attributes in separate place then
 * LocalStorage. This is the "old" default storage.
 *
 * @author cstamas
 */
@Typed(AttributeStorage.class)
@Named("fs")
@Singleton
public class DefaultFSAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
  private final ApplicationConfiguration applicationConfiguration;

  private final Marshaller marshaller;

  /**
   * The base dir.
   */
  private final File workingDirectory;

  /**
   * Instantiates a new FSX stream attribute storage.
   */
  @Inject
  public DefaultFSAttributeStorage(final ApplicationConfiguration applicationConfiguration) {
    this(applicationConfiguration, new JacksonJSONMarshaller());
  }

  public DefaultFSAttributeStorage(final ApplicationConfiguration applicationConfiguration,
                                   final Marshaller marshaller)
  {
    this.applicationConfiguration = Preconditions.checkNotNull(applicationConfiguration);
    this.marshaller = Preconditions.checkNotNull(marshaller);
    this.workingDirectory = initializeWorkingDirectory();
    getLogger().info("Default FS AttributeStorage in place, using {} marshaller.", marshaller);
  }

  public synchronized File initializeWorkingDirectory() {
    final File workingDirectory = applicationConfiguration.getWorkingDirectory("proxy/attributes-ng");

    if (workingDirectory.exists()) {
      if (!workingDirectory.isDirectory()) {
        throw new IllegalArgumentException("The attribute storage exists and is not a directory: "
            + workingDirectory.getAbsolutePath());
      }
    }
    else {
      getLogger().info("Attribute storage directory does not exists, creating it here: " + workingDirectory);

      if (!workingDirectory.mkdirs()) {
        if (!workingDirectory.isDirectory()) {
          throw new IllegalArgumentException("Could not create the attribute storage directory on path "
              + workingDirectory.getAbsolutePath());
        }
      }
    }

    return workingDirectory;
  }

  /**
   * Gets the base dir.
   *
   * @return the base dir
   */
  public File getWorkingDirectory()
      throws IOException
  {
    return workingDirectory;
  }

  // == Main iface: AttributeStorage

  public boolean deleteAttributes(final RepositoryItemUid uid) {
    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.delete);

    try {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Deleting attributes on UID=" + uid.toString());
      }

      boolean result = false;

      try {
        File ftarget = getFileFromBase(uid);

        result = ftarget.exists() && ftarget.isFile() && ftarget.delete();
      }
      catch (IOException e) {
        getLogger().warn("Got IOException during delete of UID=" + uid.toString(), e);
      }

      return result;
    }
    finally {
      uidLock.unlock();
    }
  }

  public Attributes getAttributes(final RepositoryItemUid uid) {
    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.read);

    try {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Loading attributes on UID=" + uid.toString());
      }

      try {
        return doGetAttributes(uid);
      }
      catch (IOException ex) {
        getLogger().error("Got IOException during reading of UID=" + uid.toString(), ex);

        return null;
      }
    }
    finally {
      uidLock.unlock();
    }
  }

  public void putAttributes(final RepositoryItemUid uid, Attributes attributes) {
    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.create);

    try {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Storing attributes on UID=" + uid.toString());
      }

      try {
        Attributes onDisk = doGetAttributes(uid);

        if (onDisk != null && (onDisk.getGeneration() > attributes.getGeneration())) {
          onDisk.overlayAttributes(attributes);

          // and overlay other things too
          onDisk.setRepositoryId(uid.getRepository().getId());
          onDisk.setPath(uid.getPath());
          onDisk.setReadable(attributes.isReadable());
          onDisk.setWritable(attributes.isWritable());

          attributes = onDisk;
        }

        File target = getFileFromBase(uid);

        target.getParentFile().mkdirs();

        if (target.getParentFile().exists() && target.getParentFile().isDirectory()) {
          FileOutputStream fos = null;

          try {
            fos = new FileOutputStream(target);

            attributes.incrementGeneration();

            marshaller.marshal(attributes, fos);
          }
          catch (IOException ex) {
            // NEXUS-4871 prevent zero length/corrupt files
            if (target.length() == 0) {
              target.delete();
            }
            throw ex;
          }
          finally {
            Closeables.closeQuietly(fos);
          }
        }
        else {
          getLogger().error(
              "Could not store attributes on UID=" + uid.toString()
                  + ", parent exists but is not a directory!");
        }
      }
      catch (IOException ex) {
        getLogger().error("Got IOException during store of UID=" + uid.toString(), ex);
      }
    }
    finally {
      uidLock.unlock();
    }
  }

  /**
   * Gets the file from base.
   *
   * @param uid the uid
   * @return the file from base
   */
  protected File getFileFromBase(final RepositoryItemUid uid)
      throws IOException
  {
    final File repoBase = new File(getWorkingDirectory(), uid.getRepository().getId());

    File result = null;

    String path = FileUtils.getPath(uid.getPath());

    String name = FileUtils.removePath(uid.getPath());

    result = new File(repoBase, path + "/" + name);

    // to be foolproof
    // 2007.11.09. - Believe or not, Nexus deleted my whole USB rack! (cstamas)
    // ok, now you may laugh :)
    if (!result.getAbsolutePath().startsWith(getWorkingDirectory().getAbsolutePath())) {
      throw new IOException("FileFromBase evaluated directory wrongly! baseDir="
          + getWorkingDirectory().getAbsolutePath() + ", target=" + result.getAbsolutePath());
    }
    else {
      return result;
    }
  }

  // ==

  /**
   * Gets the attributes.
   *
   * @param uid the uid
   * @return the attributes
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected Attributes doGetAttributes(final RepositoryItemUid uid)
      throws IOException
  {
    final File target = getFileFromBase(uid);

    Attributes result = null;

    boolean corrupt = false;

    if (target.exists() && target.isFile()) {
      FileInputStream fis = null;

      try {
        if (target.length() == 0) {
          // NEXUS-4871
          throw new InvalidInputException("Attribute of " + uid + " is empty!");
        }

        fis = new FileInputStream(target);

        result = marshaller.unmarshal(fis);

        result.setRepositoryId(uid.getRepository().getId());
        result.setPath(uid.getPath());

        // fixing remoteChecked
        if (result.getCheckedRemotely() == 0 || result.getCheckedRemotely() == 1) {
          result.setCheckedRemotely(System.currentTimeMillis());

          result.setExpired(true);
        }

        // fixing lastRequested
        if (result.getLastRequested() == 0) {
          result.setLastRequested(System.currentTimeMillis());
        }
      }
      catch (InvalidInputException e) {
        if (getLogger().isDebugEnabled()) {
          // we log the stacktrace
          getLogger().info("Attributes of " + uid + " are corrupt, deleting it.", e);
        }
        else {
          // just remark about this
          getLogger().info("Attributes of " + uid + " are corrupt, deleting it.");
        }

        corrupt = true;
      }
      catch (IOException e) {
        getLogger().info("While reading attributes of " + uid + " we got IOException:", e);

        throw e;
      }
      finally {
        Closeables.closeQuietly(fis);
      }
    }

    if (corrupt) {
      deleteAttributes(uid);
    }

    return result;
  }

}
