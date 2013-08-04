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

package org.sonatype.nexus.proxy.wastebasket;

import org.sonatype.nexus.proxy.repository.Repository;

public class RelativeMaximumSizeConstraint
    implements MaximumSizeConstraint
{
  private final double ratioThreshold;

  public RelativeMaximumSizeConstraint(final double ratioThreshold) {
    this.ratioThreshold = ratioThreshold;
  }

  public boolean isOverMaximum(SmartWastebasket wastebasket, Repository repository) {
    Long wastebasketSize = wastebasket.getSize(repository);

    if (wastebasketSize != null) {
      double actualRatio = wastebasketSize / 1; // repository.getStatistics().getRepositoryUsefulSize();

      return actualRatio > ratioThreshold;
    }
    else {
      return false;
    }

  }
}
