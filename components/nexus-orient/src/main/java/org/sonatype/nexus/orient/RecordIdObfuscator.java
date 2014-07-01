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
package org.sonatype.nexus.orient;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link ORID} obfuscation helper.
 *
 * @since 3.0
 */
public class RecordIdObfuscator
{
  // TODO: replace with fast encrypt/decrypt

  public static String encode(final ORID rid) {
    checkNotNull(rid);
    return Hex.encode(rid.toStream());
  }

  public static ORID decode(final String encoded) {
    checkNotNull(encoded);
    byte[] decoded = Hex.decode(encoded);
    return new ORecordId().fromStream(decoded);
  }
}
