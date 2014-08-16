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
package org.sonatype.nexus.extender.modules;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.RankingFunction;

/**
 * Provides ranking policy that gives more recent plugins priority over older plugins.
 * 
 * @since 3.0
 */
public class RankingModule
    extends AbstractModule
{
  private final AtomicInteger rank = new AtomicInteger(1);

  @Override
  protected void configure() {
    /*
     * Workaround Sisu issue where DefaultRankingFunction doesn't take account potential @Priority values
     * when calculating the maximum potential rank contained in a given Injector - this heuristic is used
     * to decide the optimal time to merge in new results when iterating over a dynamic collection.
     */
    bind(RankingFunction.class).toInstance(new RankingFunction()
    {
      private final RankingFunction function = new DefaultRankingFunction(rank.incrementAndGet());

      public <T> int rank(Binding<T> binding) {
        return function.rank(binding);
      }

      public int maxRank() {
        return Integer.MAX_VALUE; // potential max rank could be as high as MAX_VALUE if @Priority is used
      }
    });
  }
}
