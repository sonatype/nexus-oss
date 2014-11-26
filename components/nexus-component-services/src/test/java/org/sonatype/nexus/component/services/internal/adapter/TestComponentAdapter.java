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
package org.sonatype.nexus.component.services.internal.adapter;

import org.sonatype.nexus.component.services.adapter.ComponentAdapter;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Entity adapter for test components.
 */
public class TestComponentAdapter
    extends ComponentAdapter
{
  public static final String CLASS_NAME = "testcomponent";

  public static final String P_BINARY = "binaryProp";
  public static final String P_BOOLEAN = "booleanProp";
  public static final String P_BYTE = "byteProp";
  public static final String P_DATETIME = "datetimeProp";
  public static final String P_DOUBLE = "doubleProp";
  public static final String P_EMBEDDEDLIST = "embeddedlistProp";
  public static final String P_EMBEDDEDMAP = "embeddedmapProp";
  public static final String P_EMBEDDEDSET = "embeddedsetProp";
  public static final String P_FLOAT = "floatProp";
  public static final String P_INTEGER = "integerProp";
  public static final String P_LONG = "longProp";
  public static final String P_SHORT = "shortProp";
  public static final String P_STRING = "stringProp"; // required and indexed (unique)...all others are optional
  public static final String P_UNREGISTERED = "unregisteredProp"; // not a formal part of the schema...all others are

  public TestComponentAdapter() {
    super(CLASS_NAME);
  }

  @Override
  public void initClass(final OClass oClass) {
    createOptionalProperty(oClass, P_BINARY, OType.BINARY);
    createOptionalProperty(oClass, P_BOOLEAN, OType.BOOLEAN);
    createOptionalProperty(oClass, P_BYTE, OType.BYTE);
    createOptionalProperty(oClass, P_DATETIME, OType.DATETIME);
    createOptionalProperty(oClass, P_DOUBLE, OType.DOUBLE);
    createOptionalProperty(oClass, P_EMBEDDEDLIST, OType.EMBEDDEDLIST);
    createOptionalProperty(oClass, P_EMBEDDEDMAP, OType.EMBEDDEDMAP);
    createOptionalProperty(oClass, P_EMBEDDEDSET, OType.EMBEDDEDSET);
    createOptionalProperty(oClass, P_FLOAT, OType.FLOAT);
    createOptionalProperty(oClass, P_INTEGER, OType.INTEGER);
    createOptionalProperty(oClass, P_LONG, OType.LONG);
    createOptionalProperty(oClass, P_SHORT, OType.SHORT);
    createRequiredAutoIndexedProperty(oClass, P_STRING, OType.STRING, false);
    // NOTE: P_UNREGISTERED is not a formal part of the schema, but may
    // still be used because this oClass doesn't specify strict mode
  }
}
