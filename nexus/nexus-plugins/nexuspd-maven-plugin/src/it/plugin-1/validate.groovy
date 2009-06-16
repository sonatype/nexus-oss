/*
 * Copyright (C) 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// $Id: validate.groovy 8178 2009-05-12 19:21:54Z bentmann $
//
def basedir = "/Users/demers/dev/source/nexus/trunk/nexus/nexus-plugins/nexuspd-maven-plugin/target/it/plugin-1";

def outputFile = new File(basedir, 'target/classes/META-INF/plugin.xml');
def pomFile = new File(basedir, "pom.xml");

assert outputFile.exists();

def pluginModel = new XmlSlurper().parse(outputFile);

def pomModel = new XmlSlurper().parse(pomFile);

assert pluginModel.groupId == pomModel.groupId;
assert pluginModel.artifactId == pomModel.artifactId;
assert pluginModel.version == pomModel.version;
assert pluginModel.name == pomModel.name : "Expected: "+ pomModel.name +", but found: "+ pluginModel.name;
assert pluginModel.description == pomModel.description;
assert pluginModel.applicationId == "nexus";

def components = pluginModel.components.'*'.text();

//check for expected components
assert components.contains("org.sonatype.plugin.test.ComponentExtentionPoint") : "components: "+ components;
assert components.contains("org.sonatype.plugin.test.ManagedViaInterface") : "components: "+ components;
assert components.contains("org.sonatype.plugin.test.ComponentManaged") : "components: "+ components;

assert 3 == pluginModel.components.component.size();


// dependencies
assert 11 == pluginModel.classpathDependencies.classpathDependency.size() : "actual: "+ pluginModel.classpathDependencies.classpathDependency.size();

return true;
