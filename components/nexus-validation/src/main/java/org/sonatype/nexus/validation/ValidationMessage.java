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
package org.sonatype.nexus.validation;

import javax.annotation.Nullable;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validation message.
 *
 * @see ValidationResponse
 */
public class ValidationMessage
{
  private final String key;

  private final String message;

  private final Throwable cause;

  public ValidationMessage(final String key, final String message, final @Nullable Throwable cause) {
    this.key = checkNotNull(key);
    this.message = checkNotNull(message);
    this.cause = cause;
  }

  public ValidationMessage(final String key, final String message) {
    this(key, message,  null);
  }

  public String getKey() {
    return key;
  }

  public String getMessage() {
    return message;
  }

  @Nullable
  public Throwable getCause() {
    return cause;
  }

  public String toString() {
    StringBuilder buff = new StringBuilder();

    buff.append(key).append(": ").append(message);

    if (cause != null) {
      buff.append("\n    Cause: ")
          .append(Throwables.getStackTraceAsString(cause).trim());
    }

    return buff.toString();
  }
}
