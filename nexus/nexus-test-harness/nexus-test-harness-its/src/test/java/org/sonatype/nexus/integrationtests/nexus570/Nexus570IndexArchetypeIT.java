/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus570;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus570IndexArchetypeIT extends AbstractNexusIntegrationTest {

	@BeforeClass
	public void setSecureTest() {
		TestContainer.getInstance().getTestContext().setSecureTest(true);
	}

	@Test
	public void searchForArchetype() throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		args.put("a", "simple-archetype");
		args.put("g", "nexus570");

		List<NexusArtifact> results = getSearchMessageUtil().searchFor(args);

		Assert.assertEquals(1, results.size());
		Assert.assertEquals(results.get(0).getPackaging(), "maven-archetype",
				"Expected maven-archetype packaging: "
						+ results.get(0).getPackaging());

	}

	@Test
	public void searchForjar() throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		args.put("a", "normal");
		args.put("g", "nexus570");

		List<NexusArtifact> results = getSearchMessageUtil().searchFor(args);

		Assert.assertEquals(results.size(), 1);
		Assert.assertEquals(results.get(0).getPackaging(), "jar",
				"Expected jar packaging: " + results.get(0).getPackaging());

	}

}
