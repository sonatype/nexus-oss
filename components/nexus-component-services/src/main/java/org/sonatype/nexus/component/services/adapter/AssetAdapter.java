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
package org.sonatype.nexus.component.services.adapter;

import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.model.EntityId;

import com.google.common.collect.ImmutableSet;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkState;

/**
 * Adapter for the abstract "asset" storage class, which extends from "entity".
 * <p>
 * This is the base storage class of all assets. An asset represents a file that
 * logically belongs to a component, and may have format-specific properties in addition
 * to those defined below.
 * <p>
 * Subclasses are generally expected to be {@code @Named} {@code @Singleton}s, define a
 * {@code CLASS_NAME} constant, and implement a no-arg constructor that calls {@code super(CLASS_NAME)}.
 * They should also override {@link EntityAdapter#initClass(OClass)} if they need to define additional
 * properties and/or indexes as part of the storage schema.
 *
 * @since 3.0
 */
@Named
@Singleton
public class AssetAdapter
    extends EntityAdapter
{
  /** Storage class name. */
  public static final String CLASS_NAME = "asset";

  /**
   * Property name for the component in which an asset belongs.
   * This is a system-controlled property whose value is an {@link EntityId}.
   */
  public static final String P_COMPONENT = "component";

  /**
   * Property name for the date an asset was first created.
   * This is a system-controlled property whose value is a {@link DateTime}.
   */
  public static final String P_FIRST_CREATED = "firstCreated";

  /**
   * Property name for an asset's mime type.
   * This is an optional property whose value is a {@code String}.
   */
  public static final String P_CONTENT_TYPE = "contentType";

  /**
   * Property name for an asset's path.
   * This is an optional property whose value is a {@code String}.
   */
  public static final String P_PATH = "path";

  /**
   * Property name for the date an asset's content was last modified.
   * This is a system-controlled property whose value is a {@link DateTime}.
   */
  public static final String P_LAST_MODIFIED = "lastModified";

  /**
   * Property name for the length of the content in bytes.
   * This is a system-controlled property whose value is a {@link Long}.
   */
  public static final String P_CONTENT_LENGTH = "contentLength";

  /**
   * Property name for the known locations of an asset's content; a map of blob ids keyed by blobstore id.
   * This is a system-controlled property whose value is a {@code Map} with {@code String} keys and values.
   */
  public static final String P_BLOB_REFS = "blobRefs";

  /**
   * Asset properties whose values are system-controlled.
   */
  public static final Set<String> SYSTEM_PROPS =
      ImmutableSet.of(P_ID, P_COMPONENT, P_FIRST_CREATED, P_LAST_MODIFIED, P_CONTENT_LENGTH, P_BLOB_REFS);

  /**
   * No-arg constructor for direct instances.
   */
  public AssetAdapter() {
    this(CLASS_NAME);
    checkState(this.getClass() == AssetAdapter.class, "Subclass must use super(className) constructor");
  }

  /**
   * Constructor for subclasses.
   */
  protected AssetAdapter(String className) {
    super(className);
  }

  @Override
  public OClass getClass(OSchema schema) {
    OClass superClass = super.getClass(schema);
    OClass oClass = schema.getClass(CLASS_NAME);
    if (oClass == null) {
      oClass = schema.createAbstractClass(CLASS_NAME, superClass);
      createRequiredAutoIndexedLinkProperty(oClass, P_COMPONENT, OType.LINK, superClass, false);
      createRequiredProperty(oClass, P_FIRST_CREATED, OType.DATETIME);
      createOptionalProperty(oClass, P_CONTENT_TYPE, OType.STRING);
      createOptionalProperty(oClass, P_PATH, OType.STRING);
      createRequiredProperty(oClass, P_BLOB_REFS, OType.EMBEDDEDMAP);
      logCreatedClassInfo(oClass);
    }
    maybeCreateSubClass(schema, oClass, AssetAdapter.class);
    return oClass;
  }
}
