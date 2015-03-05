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

package org.sonatype.nexus.commands.completers;

import java.lang.reflect.Field;

import javax.inject.Named;

import org.sonatype.nexus.commands.CompleterTargetAware;

import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Enum} completer.
 *
 * @since 3.0
 */
@Named("enum")
public class EnumCompleter
    extends StringsCompleter
    implements CompleterTargetAware
{
  private static final Logger log = LoggerFactory.getLogger(EnumCompleter.class);

  public EnumCompleter() {
    super(true); // case-sensitive

    // NOTE: strings installed when completer target is provided
  }

  @Override
  public void setCompleterTarget(final Field field) {
    Class<?> type = field.getType();
    if (type.isEnum()) {
      for (Object constant : type.getEnumConstants()) {
        getStrings().add(String.valueOf(constant));
      }
    }
    else {
      log.warn("Completer target field is not an enum: {}", field);
    }
  }
}
