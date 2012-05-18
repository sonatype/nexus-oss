/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugin.settings;

import org.codehaus.plexus.interpolation.Interpolator;

/**
 * Allows customization of the template {@link Interpolator}.
 *
 * @since 2.1
 */
public interface TemplateInterpolatorCustomizer
{
    void customize(DownloadSettingsTemplateMojo owner, Interpolator interpolator);
}
