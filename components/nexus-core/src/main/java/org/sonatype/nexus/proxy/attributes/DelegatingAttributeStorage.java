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

import java.io.IOException;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;

import com.google.common.base.Preconditions;

/**
 * Simple wrapping AttributeStorage that delegates only when needed.
 *
 * @author cstamas
 * @since 2.0
 */
public class DelegatingAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
  private final AttributeStorage delegate;

  public DelegatingAttributeStorage(final AttributeStorage delegate) {
    this.delegate = Preconditions.checkNotNull(delegate);
  }

  public AttributeStorage getDelegate() {
    return delegate;
  }

  @Override
  public Attributes getAttributes(RepositoryItemUid uid)
      throws IOException
  {
    if (isMetadataMaintained(uid)) {
      return delegate.getAttributes(uid);
    }

    return null;
  }

  @Override
  public void putAttributes(RepositoryItemUid uid, Attributes attributes)
      throws IOException
  {
    if (isMetadataMaintained(uid)) {
      delegate.putAttributes(uid, attributes);
    }
  }

  @Override
  public boolean deleteAttributes(RepositoryItemUid uid)
      throws IOException
  {
    if (isMetadataMaintained(uid)) {
      return delegate.deleteAttributes(uid);
    }

    return false;
  }

  // ==

  /**
   * Returns true if the attributes should be maintained at all.
   *
   * @return true if attributes should exists for given UID.
   */
  protected boolean isMetadataMaintained(final RepositoryItemUid uid) {
    Boolean isMetadataMaintained = uid.getAttributeValue(IsMetadataMaintainedAttribute.class);

    if (isMetadataMaintained != null) {
      return isMetadataMaintained.booleanValue();
    }
    else {
      // safest
      return true;
    }
  }
}
