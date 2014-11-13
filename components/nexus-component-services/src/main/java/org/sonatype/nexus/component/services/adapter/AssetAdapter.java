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
package org.sonatype.nexus.component.services.adapter;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.google.common.base.Predicate;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapter for converting between {@link Asset} objects and OrientDB {@code ODocument}s.
 *
 * @since 3.0
 */
public abstract class AssetAdapter<T extends Asset>
    extends EntityAdapterSupport
    implements EntityAdapter<T>
{
  /** OrientDB base class name for assets. */
  public static final String BASE_CLASS_NAME = new OClassNameBuilder().type(Asset.class).build();

  /** OrientDB property name for the component in which an asset belongs. */
  public static final String P_COMPONENT = "component";

  /** OrientDB property name for the date an asset was first created. */
  public static final String P_FIRST_CREATED = "firstCreated";

  /** OrientDB property name for an asset's mime type. */
  public static final String P_CONTENT_TYPE = "contentType";

  /** OrientDB property name for an asset's path. */
  public static final String P_PATH = "path";

  /** OrientDB property name for the known locations of an asset's content; a map of blob ids keyed by blobstore id. */
  public static final String P_BLOB_REFS = "blobRefs";

  @Override
  public void createStorageClass(OSchema schema) {
    createAndInitStorageClassWithBaseClass(
        checkNotNull(schema),
        BASE_CLASS_NAME,
        new Predicate<OClass>() {
          @Override
          public boolean apply(final OClass baseClass) {
            createRequiredAutoIndexedProperty(baseClass, P_ID, OType.STRING, true);
            createRequiredAutoIndexedProperty(baseClass, P_COMPONENT, OType.LINK, false);
            createRequiredProperty(baseClass, P_FIRST_CREATED, OType.DATETIME);
            createOptionalProperty(baseClass, P_CONTENT_TYPE, OType.STRING);
            createOptionalProperty(baseClass, P_PATH, OType.STRING);
            createRequiredProperty(baseClass, P_BLOB_REFS, OType.EMBEDDEDMAP);
            return true;
          }
        },
        new OClassNameBuilder().type(getEntityClass()).build(),
        new Predicate<OClass>() {
          @Override
          public boolean apply(final OClass storageClass) {
            initStorageClass(storageClass);
            return true;
          }
        });
  }

  /**
   * Initializes the given OrientDB class with properties and associated indexes to store entities of this type.
   */
  protected abstract void initStorageClass(OClass storageClass);
}
