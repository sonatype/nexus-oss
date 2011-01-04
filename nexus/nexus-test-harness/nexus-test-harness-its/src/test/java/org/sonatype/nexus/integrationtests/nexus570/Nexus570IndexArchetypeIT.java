/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
