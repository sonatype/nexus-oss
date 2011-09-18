/*
 * Sonatype Overlord (TM)
 * Copyright (C) 2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/overlord/attributions/.
 * "Sonatype" and "Sonatype Overlord" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.bundle.launcher;

import org.sonatype.sisu.bl.Bundle;

/**
 * An Nexus bundle that can be created, started, stopped based on a provided configuration.
 *
 * @since 1.9.3
 */
public interface NexusBundle
        extends Bundle<NexusBundle, NexusBundleConfiguration> {

}
