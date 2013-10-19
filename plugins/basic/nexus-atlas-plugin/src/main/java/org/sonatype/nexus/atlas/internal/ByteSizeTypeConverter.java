/*
 * Copyright (c) 2008-2013 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.atlas.internal;

import javax.inject.Named;

import org.sonatype.sisu.goodies.common.ByteSize;

import com.google.inject.spi.TypeConverter;

/**
 * Guice {@link TypeConverter} for {@link ByteSize} instances.
 *
 * @since 2.7
 */
@Named
public class ByteSizeTypeConverter
    extends org.sonatype.sisu.goodies.inject.converter.ByteSizeTypeConverter
{
  // HACK: Work-around for type-converters not getting picked up by the container
}
