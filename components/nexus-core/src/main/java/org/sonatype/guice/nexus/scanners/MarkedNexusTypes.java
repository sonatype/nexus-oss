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

package org.sonatype.guice.nexus.scanners;

import java.lang.annotation.Annotation;

enum MarkedNexusTypes
    implements NexusType
{
  // ----------------------------------------------------------------------
  // Values
  // ----------------------------------------------------------------------

  UNKNOWN
      {
        @Override
        public boolean isComponent() {
          return false;
        }
      },

  EXTENSION_POINT
      {
        @Override
        public boolean isSingleton() {
          return false;
        }

        @Override
        public NexusType asSingleton() {
          return EXTENSION_POINT_SINGLETON;
        }
      },

  EXTENSION_POINT_SINGLETON,

  MANAGED
      {
        @Override
        public boolean isSingleton() {
          return false;
        }

        @Override
        public NexusType asSingleton() {
          return MANAGED_SINGLETON;
        }
      },

  MANAGED_SINGLETON;

  // ----------------------------------------------------------------------
  // Common methods
  // ----------------------------------------------------------------------

  public boolean isComponent() {
    return true;
  }

  public boolean isSingleton() {
    return true;
  }

  public NexusType asSingleton() {
    return this;
  }

  public final Annotation details() {
    return null;
  }
}