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

package org.sonatype.nexus.scheduling;

import java.util.Set;

/**
 * An activity descriptor for tasks that are running against one or set of Repositories.
 *
 * @author cstamas
 */
public interface RepositoryTaskActivityDescriptor
    extends TaskActivityDescriptor
{
  /**
   * Regarding repository content: create = creates new files/content in repository, update = modifies the content of
   * existing content, delete = removes content from repository.
   *
   * @author cstamas
   */
  public enum ModificationOperator
  {
    create, update, delete
  }

  ;

  /**
   * Regarding repository attributes (they are 1:1 to content always): extend = creates new values in attributes (ie.
   * adds custom attributes), refresh = updates/freshens current attributes, lessen = removes existing attributes.
   *
   * @author cstamas
   */
  public enum AttributesModificationOperator
  {
    extend, refresh, lessen
  }

  ;

  /**
   * Returns true if it will scan/walk repository.
   */
  boolean isScanningRepository();

  /**
   * Will scan/walk the repository. Naturally, it implies READ operation happening against repository. If returned
   * null, it will not scan.
   */
  String getRepositoryScanningStartingPath();

  /**
   * Will apply these <b>modify</b> operations to the content of the repository.
   */
  Set<ModificationOperator> getContentModificationOperators();

  /**
   * Will apply these attribute <b>modify</b> operations to the attributes of the repository.
   */
  Set<AttributesModificationOperator> getAttributesModificationOperators();
}
