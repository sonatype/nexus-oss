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

package org.sonatype.nexus.coreui

import groovy.transform.ToString
import org.sonatype.aether.util.version.GenericVersionScheme

/**
 * Search result exchange object.
 *
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class SearchResultVersionXO
implements Comparable<SearchResultVersionXO>
{
  private static final versionScheme = new GenericVersionScheme()

  String groupId
  String artifactId
  String version
  Integer versionOrder
  String repositoryId
  String repositoryName
  String path
  String name
  String type

  boolean equals(final o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    SearchResultVersionXO that = (SearchResultVersionXO) o

    if (path != that.path) return false
    if (repositoryId != that.repositoryId) return false
    if (version != that.version) return false

    return true
  }

  int hashCode() {
    int result
    result = version.hashCode()
    result = 31 * result + repositoryId.hashCode()
    result = 31 * result + path.hashCode()
    return result
  }

  @Override
  int compareTo(final SearchResultVersionXO o) {
    def result = repositoryName.compareTo(o.repositoryName)
    if (result == 0) {
      result = versionScheme.parseVersion(version).compareTo(versionScheme.parseVersion(o.version))
      if (result == 0) {
        result = path.compareTo(o.path)
      }
    }
    return result
  }

}
