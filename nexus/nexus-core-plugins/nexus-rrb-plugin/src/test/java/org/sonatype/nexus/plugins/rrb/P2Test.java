package org.sonatype.nexus.plugins.rrb;

import org.junit.Test;

public class P2Test {
	//What is the format of the P2?
	//Is it maybe only an API?
	//Support for P2 in Nexus? Which classes, how, ... best practices...
	
	@Test
	public void testFetch() {
		
	}
	
	String mylunRepo() {
		//http://download.eclipse.org/tools/mylyn/update/e3.4/
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--" +
		"    Copyright (c) 2009 Tasktop Technologies and others." +
		"    All rights reserved. This program and the accompanying materials" +
		"    are made available under the terms of the Eclipse Public License v1.0" +
		"    which accompanies this distribution, and is available at" +
		"    http://www.eclipse.org/legal/epl-v10.html" +
		"   " +
		"    Contributors:" +
		"         Tasktop Technologies - initial API and implementation" +
		" -->" +
		"" +
		"<site pack200=\"true\">" +
		"   <description url=\"http://download.eclipse.org/tools/mylyn/update/e3.4\">" +
		"      Mylyn Update for Eclipse 3.4, 3.5 and 3.6" +
		"   </description>" +
		"   <feature url=\"features/org.eclipse.mylyn_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Features\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.context_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.context_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Features\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.ide_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.ide_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.java_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.java_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.cdt.mylyn_5.2.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.cdt.mylyn\" version=\"5.2.1.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.team_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.team_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.pde_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.pde_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.bugzilla_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.bugzilla_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.wikitext_feature_1.2.0.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.wikitext_feature\" version=\"1.2.0.v20091215-0000-e3x\">" +
		"      <category name=\"Integration\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.sdk_feature_3.3.1.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.sdk_feature\" version=\"3.3.1.v20091215-0000-e3x\">" +
		"      <category name=\"SDK\"/>" +
		"   </feature>" +
		"   <feature url=\"features/org.eclipse.mylyn.wikitext.sdk_1.2.0.v20091215-0000-e3x.jar\" id=\"org.eclipse.mylyn.wikitext.sdk\" version=\"1.2.0.v20091215-0000-e3x\">" +
		"      <category name=\"SDK\"/>" +
		"   </feature>" +
		"   <category-def name=\"Features\" label=\"Mylyn Features\">" +
		"      <description>" +
		"         The Task List and the Task-Focused Interface." +
		"      </description>" +
		"   </category-def>" +
		"   <category-def name=\"Integration\" label=\"Mylyn Integration\">" +
		"      <description>" +
		"         Mylyn bridges that integrate with documents and programming languages. Mylyn connectors that integrate with task repositories." +
		"      </description>" +
		"   </category-def>" +
		"   <category-def name=\"SDK\" label=\"Mylyn Plug-in Development\">" +
		"      <description>" +
		"         Source code and documentation for integrators building on Mylyn." +
		"      </description>" +
		"   </category-def>" +
		"</site>";
	}

}
