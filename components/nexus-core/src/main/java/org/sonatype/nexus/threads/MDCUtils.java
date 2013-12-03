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

package org.sonatype.nexus.threads;

import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Simple helper class to manipulate MDC.
 *
 * @author cstamas
 * @since 2.6
 */
public class MDCUtils
{
  private static final Logger log = LoggerFactory.getLogger(MDCUtils.class);

  public static final String CONTEXT_NON_INHERITABLE_KEY = "non-inheritable";

  public static final String USER_ID_KEY = "userId";

  public static final String UNKNOWN_USER_ID = "<unknown-user>";

  public static void setMDCUserIdIfNeeded() {
    final String userId = MDC.get(USER_ID_KEY);
    if (Strings.isNullOrEmpty(userId)) {
      setMDCUserId();
    }
  }

  public static void setMDCUserId() {
    MDC.put(USER_ID_KEY, getCurrentUserId());
  }

  public static void unsetMDCUserId() {
    MDC.remove(USER_ID_KEY);
  }

  public static String getCurrentUserId() {
    String userId = UNKNOWN_USER_ID;
    try {
      final Subject subject = SecurityUtils.getSubject();
      if (subject != null) {
        final Object principal = subject.getPrincipal();
        if (principal != null) {
          userId = principal.toString();
        }
      }
    }
    catch (Exception e) {
      log.warn("Unable to determine current user; ignoring", e);
    }
    log.trace("Current userId: {}", userId);
    return userId;
  }

  // ==

  public static Map<String, String> getCopyOfContextMap() {
    final boolean inheritable = MDC.get(CONTEXT_NON_INHERITABLE_KEY) == null;
    Map<String, String> result = null;
    if (inheritable) {
      result = MDC.getCopyOfContextMap();
    }
    if (result == null) {
      result = Maps.newHashMap();
    }
    result.remove(CONTEXT_NON_INHERITABLE_KEY);
    return result;
  }

  public static void setContextMap(Map<String, String> context) {
    if (context != null) {
      MDC.setContextMap(context);
      setMDCUserIdIfNeeded();
    }
    else {
      MDC.clear();
      setMDCUserId();
    }
  }
}
