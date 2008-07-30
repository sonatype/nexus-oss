package org.sonatype.nexus.integrationtests.nexus383;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;

public class Nexus383Search extends AbstractNexusIntegrationTest {

	protected SearchMessageUtil messageUtil;

	public Nexus383Search() {
		this.messageUtil = new SearchMessageUtil(this.getBaseNexusUrl());
	}

	@Test
	public void searchForGroupId() throws Exception {
		List<NexusArtifact> results = messageUtil.searchFor("nexus383");
		Assert.assertEquals(2, results.size());

		results = messageUtil.searchFor("nexus-383");
		Assert.assertTrue(results.isEmpty());
	}

	@Test
	public void searchForArtifactId() throws Exception {
		List<NexusArtifact> results = messageUtil.searchFor("know-artifact-1");
		Assert.assertEquals(1, results.size());

		results = messageUtil.searchFor("know-artifact-2");
		Assert.assertEquals(1, results.size());

		results = messageUtil.searchFor("know-artifact");
		Assert.assertEquals(2, results.size());

		results = messageUtil.searchFor("unknow-artifacts");
		Assert.assertTrue(results.isEmpty());
	}

}
