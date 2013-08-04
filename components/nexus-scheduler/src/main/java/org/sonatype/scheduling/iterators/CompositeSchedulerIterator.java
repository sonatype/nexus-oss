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

package org.sonatype.scheduling.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CompositeSchedulerIterator
    extends AbstractSchedulerIterator
{
  private final List<SchedulerIterator> iterators;

  public CompositeSchedulerIterator(Collection<SchedulerIterator> its) {
    super(new Date(), null);

    this.iterators = new ArrayList<SchedulerIterator>(its.size());

    this.iterators.addAll(its);
  }

  @Override
  protected Date doPeekNext() {
    // get the "smallest" date and return it's peekNext();
    return getNextIterator().peekNext();
  }

  @Override
  public void stepNext() {
    // get the "smallest" date and return it's next();
    getNextIterator().next();
  }

  @Override
  public boolean isFinished() {
    // it is finished if all iterators are finished
    boolean result = false;

    for (SchedulerIterator i : iterators) {
      result = result || i.isFinished();
    }

    return result;
  }

  protected SchedulerIterator getNextIterator() {
    Date currDate = null;

    Date nextDate = null;

    SchedulerIterator currIterator = null;

    SchedulerIterator nextIterator = null;

    for (Iterator<SchedulerIterator> i = iterators.iterator(); i.hasNext(); ) {
      currIterator = i.next();

      currDate = currIterator.peekNext();

      if (currDate == null) {
        i.remove();
      }
      else {
        if (nextDate == null || currDate.before(nextDate)) {
          nextDate = currDate;

          nextIterator = currIterator;
        }
      }
    }
    return nextIterator;
  }

  public void resetFrom(Date from) {
    for (SchedulerIterator iter : iterators) {
      iter.resetFrom(from);
    }
  }
}
