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

package org.sonatype.nexus.configuration;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.model.CProps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A simple CProps to Map converter, to ease handling of CProps.
 *
 * @author cstamas
 */
public class ModelloUtils
{
  public static Map<String, String> getMapFromConfigList(List<CProps> list) {
    final Map<String, String> result = Maps.newHashMapWithExpectedSize(list.size());
    for (CProps props : list) {
      result.put(props.getKey(), props.getValue());
    }
    return result;
  }

  public static List<CProps> getConfigListFromMap(final Map<String, String> map) {
    final List<CProps> result = Lists.newArrayListWithExpectedSize(map.size());
    for (Map.Entry<String, String> entry : map.entrySet()) {
      final CProps cprop = new CProps();
      cprop.setKey(entry.getKey());
      cprop.setValue(entry.getValue());
      result.add(cprop);
    }
    return result;
  }
}
