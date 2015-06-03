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
package org.sonatype.nexus.orient.entity;

import javax.annotation.Nonnull;

import org.sonatype.nexus.common.entity.EntityId;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Attached {@link EntityId}.
 *
 * An {@link EntityId} that remains connected to the underlying ODocument.
 * This is necessary with OrientDb transactions mode, as ODocument ids change when the transaction is committed.
 *
 * @since 3.0
 */
public class AttachedEntityId
    implements EntityId
{
  private final EntityAdapter owner;

  private final ODocument document;

  private String value;

  public AttachedEntityId(final EntityAdapter owner, final ODocument document) {
    this.owner = checkNotNull(owner);
    this.document = checkNotNull(document);
  }

  public ORID getIdentity() {
    return document.getIdentity();
  }

  @Nonnull
  @Override
  public String getValue() {
    if (value == null) {
      ORID rid = document.getIdentity();
      checkState(!rid.isTemporary(), "attempted use of temporary/uncommitted document id");
      value = owner.getRecordIdObfuscator().encode(owner.getType(), rid);
    }
    return value;
  }
}
