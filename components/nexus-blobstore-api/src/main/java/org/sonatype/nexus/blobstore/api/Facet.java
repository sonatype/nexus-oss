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
package org.sonatype.nexus.blobstore.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * since @3.0
 */
public interface Facet<T>
{

  /**
   * Initialize facet.
   * 
   * Called when facet is attached to target.
   */
  void init(T target) throws Exception;

  /**
   * Start facet.
   * 
   * Facet has been previously initialized.
   */
  void start() throws Exception;

  /**
   * Stop facet.
   * 
   * Facet was previously started. 
   */
  void stop() throws Exception;

  /**
   * Delete facet.
   * 
   * Allows facet to cope with BlobStore being deleted.
   * 
   * Facet must have been previously stopped. 
   */
  void delete() throws Exception;

  /**
   * Marks {@link Facet} interface and/or implementation as exposed.
   *
   * Exposed types can be accessed via {@link BlobStore#facet}
   */
  @Documented
  @Target(TYPE)
  @Retention(RUNTIME)
  @interface Exposed
  {
    // marker
  }
}
