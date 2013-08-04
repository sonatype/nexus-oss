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

package org.sonatype.nexus.util;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.walker.ParentOMatic;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Simple collection of some static path related code.
 * <p>
 * Note: all the input and output paths are expected to be "normalized ones": being absolute, using generic "/"
 * character as path separator (since these are NOT FS File paths, but just hierarchical paths of strings). For
 * example:
 * {@link RepositoryItemUid#getPath()} returns paths like these and as those are used throughout of Nexus.
 * <p>
 *
 * @author cstamas
 * @since 2.4
 */
public class PathUtils
{
  /**
   * Returns the "depth" (like directory depth) on the passed in path.
   *
   * @return the depth of the path.
   */
  public static int depthOf(final String path) {
    return elementsOf(path).size();
  }

  /**
   * Splits the passed in path into path elements. Note: this code was originally in
   * {@link ParentOMatic#getPathElements} method!
   *
   * @return list of path elements.
   */
  public static List<String> elementsOf(final String path) {
    final List<String> result = Lists.newArrayList();
    final String[] elems = path.split("/");
    for (String elem : elems) {
      if (!Strings.isNullOrEmpty(elem)) {
        result.add(elem);
      }
    }
    return result;
  }

  /**
   * Assembles a path from all elements.
   *
   * @param elements the list of path elements to assemble path from.
   * @return a normalized path assembled from all path elements.
   */
  public static String pathFrom(final List<String> elements) {
    return pathFrom(elements, elements.size());
  }

  /**
   * Assembles a path from all elements.
   *
   * @param elements the list of path elements to assemble path from.
   * @param f        function to apply to path elements.
   * @return a normalized path assembled from all path elements.
   */
  public static String pathFrom(final List<String> elements, final Function<String, String> f) {
    return pathFrom(elements, elements.size(), f);
  }

  /**
   * Assembles a path from some count of elements.
   *
   * @param elements         the list of path elements to assemble path from.
   * @param maxElementsToUse has effect only if less then {@code elements.size()} naturally.
   * @return a normalized path assembled from maximized count of path elements.
   */
  public static String pathFrom(final List<String> elements, final int maxElementsToUse) {
    return pathFrom(elements, maxElementsToUse, new Ident());
  }

  /**
   * Assembles a path from some count of elements.
   *
   * @param elements         the list of path elements to assemble path from.
   * @param maxElementsToUse has effect only if less then {@code elements.size()} naturally.
   * @return a normalized path assembled from maximized count of path elements.
   */
  public static String pathFrom(final List<String> elements, final int maxElementsToUse,
                                final Function<String, String> f)
  {
    final StringBuilder sb = new StringBuilder("/");
    int elementsUsed = 0;
    final Iterator<String> elementsIterator = elements.iterator();
    while (elementsIterator.hasNext()) {
      sb.append(f.apply(elementsIterator.next()));
      elementsUsed++;
      if (elementsUsed == maxElementsToUse) {
        break;
      }
      if (elementsIterator.hasNext()) {
        sb.append("/");
      }
    }
    return sb.toString();
  }

  /**
   * Ident function for path elements.
   */
  public static final class Ident
      implements Function<String, String>
  {
    @Override
    public String apply(@Nullable String input) {
      return input;
    }
  }
}
