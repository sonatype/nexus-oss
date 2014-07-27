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
package org.sonatype.nexus.extender;

import com.google.inject.Module;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.SisuBundlePlan;
import org.osgi.framework.Bundle;

/**
 * Adapts Sisu's default plan to use {@link NexusBundleModule} for configuration.
 * 
 * @since 3.0
 */
public class NexusBundlePlan
    extends SisuBundlePlan
{
  public NexusBundlePlan(final MutableBeanLocator locator) {
    super(locator);
  }

  public static boolean hasComponents(Bundle bundle) {
    return bundle.getResource("META-INF/sisu/javax.inject.Named") != null
        || bundle.getResource("META-INF/plexus/components.xml") != null;
  }

  @Override
  protected boolean appliesTo(Bundle bundle) {
    return hasComponents(bundle);
  }

  @Override
  protected Module compose(Bundle bundle) {
    return new NexusBundleModule(bundle, locator);
  }
}
