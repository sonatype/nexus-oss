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
import java.io.IOException;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCompositeFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;

import com.google.common.io.Closeables;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import org.codehaus.plexus.util.FileUtils;

/**
 * Legacy AttributeStorage implementation that uses it's own FS storage to store attributes, by persisting StorageItem
 * as whole using XStream. This is the "old" default storage, used in all Nexuses up to version 1.10.0. Note: this
 * component is ReadOnly and should be used to perform transitioning upgrade from older Nexus instances. Only
 * {@link #deleteAttributes(RepositoryItemUid)} and {@link #getAttributes(RepositoryItemUid)} will do anything, while
 * {@link #putAttributes(RepositoryItemUid, Attributes)} will throw exception. In case the "legacy" attribute directory
 * is not present (ie. new install), this component remains dormant.
 *
 * @author cstamas
 * @since 2.0
 */
@Typed(AttributeStorage.class)
@Named("legacy")
@Singleton
public class LegacyFSAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
  private final ApplicationConfiguration applicationConfiguration;

  private final XStream marshaller;

  /**
   * The base dir.
   */
  private final File workingDirectory;

  /**
   * Instantiates a new FSX stream attribute storage.
   */
  @Inject
  public LegacyFSAttributeStorage(final ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration;
    this.workingDirectory = initializeWorkingDirectory();
    this.marshaller = new XStream();
    this.marshaller.alias("file", DefaultStorageFileItem.class);
    this.marshaller.alias("compositeFile", DefaultStorageCompositeFileItem.class);
    this.marshaller.alias("collection", DefaultStorageCollectionItem.class);
    this.marshaller.alias("link", DefaultStorageLinkItem.class);
  }

  // == Config

  public synchronized File initializeWorkingDirectory() {
    final File workingDirectory = applicationConfiguration.getWorkingDirectory("proxy/attributes", false);

    if (workingDirectory.exists()) {
      if (!workingDirectory.isDirectory()) {
        throw new IllegalArgumentException("The attribute storage exists and is not a directory: "
            + workingDirectory.getAbsolutePath());
      }

      getLogger().debug(
          "Legacy Attribute storage directory does exists here \"{}\", legacy AttributeStorage will be used.",
          workingDirectory);
    }
    else {
      getLogger().debug(
          "Legacy Attribute storage directory does not exists, was expecting it here \"{}\", legacy AttributeStorage will not be used.",
          workingDirectory);

      return null;
    }

    return workingDirectory;
  }

  public boolean isLegacyAttributeStorageDiscovered() {
    return workingDirectory != null;
  }

  public File getWorkingDirectory() {
    return workingDirectory;
  }

  // == Main iface: AttributeStorage

  public boolean deleteAttributes(final RepositoryItemUid uid) {
    if (!isLegacyAttributeStorageDiscovered()) {
      // noop
      return false;
    }

    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.delete);

    try {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Deleting attributes on UID=" + uid.toString());
      }

      boolean result = false;

      try {
        File ftarget = getFileFromBase(uid, workingDirectory);

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
    if (!isLegacyAttributeStorageDiscovered()) {
      // noop
      return null;
    }

    final RepositoryItemUidLock uidLock = uid.getLock();

    uidLock.lock(Action.read);

    try {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Loading attributes on UID=" + uid.toString());
      }

      try {
        AbstractStorageItem result = null;

        result = doGetAttributes(uid, workingDirectory);
        if (result == null) {
          return null;
        }
        else {
          return result.getRepositoryItemAttributes();
        }
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

  public void putAttributes(final RepositoryItemUid uid, final Attributes item) {
    throw new UnsupportedOperationException("Legacy AttributeStorage is read only!");
  }

  /**
   * Gets the file from base.
   *
   * @param uid the uid
   * @return the file from base
   */
  protected File getFileFromBase(final RepositoryItemUid uid, final File workingDirectory)
      throws IOException
  {
    final File repoBase = new File(workingDirectory, uid.getRepository().getId());

    File result = null;

    String path = FileUtils.getPath(uid.getPath());

    String name = FileUtils.removePath(uid.getPath());

    result = new File(repoBase, path + "/" + name);

    // to be foolproof
    // 2007.11.09. - Believe or not, Nexus deleted my whole USB rack! (cstamas)
    // ok, now you may laugh :)
    if (!result.getAbsolutePath().startsWith(workingDirectory.getAbsolutePath())) {
      throw new IOException("FileFromBase evaluated directory wrongly! baseDir="
          + workingDirectory.getAbsolutePath() + ", target=" + result.getAbsolutePath());
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
  protected AbstractStorageItem doGetAttributes(final RepositoryItemUid uid, final File workingDirectory)
      throws IOException
  {
    final File target = getFileFromBase(uid, workingDirectory);

    AbstractStorageItem result = null;

    boolean corrupt = false;

    if (target.exists() && target.isFile()) {
      FileInputStream fis = null;

      try {
        fis = new FileInputStream(target);

        result = (AbstractStorageItem) marshaller.fromXML(fis);
        result.upgrade();

        result.setRepositoryItemUid(uid);

        // fixing remoteChecked
        if (result.getRemoteChecked() == 0 || result.getRemoteChecked() == 1) {
          result.setRemoteChecked(System.currentTimeMillis());

          result.setExpired(true);
        }

        // fixing lastRequested
        if (result.getLastRequested() == 0) {
          result.setLastRequested(System.currentTimeMillis());
        }
      }
      catch (NullPointerException e) {
        // see NEXUS-3911: XPP3 throws sometimes NPE on "corrupted XMLs in some specific way"
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
      catch (XStreamException e) {
        // it is corrupt -- so says XStream, but see above and NEXUS-3911
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
