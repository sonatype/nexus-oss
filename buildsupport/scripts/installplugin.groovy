/**
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

//
// Helper to [re]install a plugin bundle into a nexus installation.
//
// Usage:
//
//    groovy -Dinstall=<nexus-install-directory> -Dplugin=<nexus-plugin-bundle-zip> ./buildsupport/scripts/installplugin.groovy
//

def pluginFile = System.getProperty('plugin') as File
assert pluginFile && pluginFile.exists()

def installDir = System.getProperty('install') as File
assert installDir && installDir.exists()

def repoDir = new File(installDir, 'nexus/WEB-INF/plugin-repository')

def ant = new AntBuilder()

// strip off -bundle.zip
assert pluginFile.name.endsWith('-bundle.zip')
def pluginPrefix = pluginFile.name[0..-12]

// delete the plugin dir, if it exists
def pluginDir = new File(repoDir, pluginPrefix)
if (pluginDir.exists()) {
  ant.delete(dir: pluginDir)
}

// unzip the new plugin
ant.unzip(src: pluginFile, dest: repoDir)
