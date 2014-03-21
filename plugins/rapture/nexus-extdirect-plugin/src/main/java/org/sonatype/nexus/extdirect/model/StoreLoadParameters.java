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
package org.sonatype.nexus.extdirect.model;

import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Ext Store load parameters.
 *
 * @since 3.0
 */
public class StoreLoadParameters
{

  private List<Filter> filter;


  public String getFilter(String property) {
    Preconditions.checkNotNull(property, "property");
    if (filter != null) {
      for (Filter item : filter) {
        if (property.equals(item.getProperty())) {
          return item.getValue();
        }
      }
    }
    return null;
  }

  public static class Filter
  {

    private String property;

    private String value;

    public String getProperty() {
      return property;
    }

    public void setProperty(final String property) {
      this.property = property;
    }

    public String getValue() {
      return value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

  }


}
