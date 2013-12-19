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

package org.sonatype.nexus.proxy.maven;

import java.util.List;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M2GavCalculator;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * Dummy LayoutConverterShadowRepository created for
 * https://issues.sonatype.org/browse/NEXUS-6185 in order to test the method
 * LayoutConverterShadowRepository.transformM2toM1() without needing a real
 * repository.
 * 
 * NEXUS-6185 Timestamped M2 Snapshots should be consumable over M1 shadow
 * 
 * @author J. Godau of Schütze Consulting AG, Berlin, DE
 */
public class Nexus6185LayoutConverterShadowRepository extends
		LayoutConverterShadowRepository {

	@Override
	public GavCalculator getM2GavCalculator() {
		return new M2GavCalculator();
	}

	@Override
	public GavCalculator getGavCalculator() {
		throw new RuntimeException("method not implemented");
	}

	@Override
	public ContentClass getRepositoryContentClass() {
		throw new RuntimeException("method not implemented");
	}

	@Override
	public ContentClass getMasterRepositoryContentClass() {
		throw new RuntimeException("method not implemented");
	}

	@Override
	public boolean isMavenMetadataPath(final String path) {
		throw new RuntimeException("method not implemented");
	}

	@Override
	protected List<String> transformMaster2Shadow(final String path) {
		throw new RuntimeException("method not implemented");
	}

	@Override
	protected List<String> transformShadow2Master(final String path) {
		throw new RuntimeException("method not implemented");
	}

	@Override
	protected Configurator getConfigurator() {
		throw new RuntimeException("method not implemented");
	}

	@Override
	protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
		throw new RuntimeException("method not implemented");
	}
}