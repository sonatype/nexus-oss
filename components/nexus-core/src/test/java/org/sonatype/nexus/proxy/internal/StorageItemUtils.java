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

package org.sonatype.nexus.proxy.internal;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.item.StorageItem;

public class StorageItemUtils
{

  public static void printStorageItemList(List<StorageItem> items) {
    PrintWriter pw = new PrintWriter(System.out);
    pw.println(" *** List of StorageItems:");
    for (StorageItem item : items) {
      printStorageItem(pw, item);
    }
    pw.println(" *** List of StorageItems end");
    pw.flush();
  }

  public static void printStorageItem(StorageItem item) {
    printStorageItem(new PrintWriter(System.out), item);
  }

  public static void printStorageItem(PrintWriter pw, StorageItem item) {
    pw.println(item.getClass().getName());
    Map<String, String> dataMap = item.getRepositoryItemAttributes().asMap();
    for (String key : dataMap.keySet()) {
      pw.print(key);
      pw.print(" = ");
      pw.print(dataMap.get(key));
      pw.println();
    }
    pw.println();
    pw.flush();
  }

}
