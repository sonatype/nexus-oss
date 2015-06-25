/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

def siestaDir = new File(project.basedir as File, '/src/test/ft-resources/testsuite/lib/siesta')
def preparedFile = new File(siestaDir, '.prepared')

if (preparedFile.exists()) {
  println "Already prepared Siesta-JS in: $siestaDir"
  return
}

println "Preparing Siesta-JS in: $siestaDir"

// Extract siesta resources, executable scripts and binaries.
// HACK: This is extracted into src/test/ft-resources for NEXUS_RESOURCE_DIRS loading support
ant.mkdir(dir: siestaDir)
ant.unzip(src: "${project.build.directory}/dependency/siesta-standard.zip", dest: siestaDir) {
  cutdirsmapper(dirs: 1)
  patternset {
    include(name: '*/resources/**')
    include(name: '*/siesta-all.js')
    include(name: '*/siesta-coverage-all.js')
    include(name: '*/bin/**')
  }
}

// Ensure scripts and binaries are executable
ant.chmod(perm: 'u+x') {
  fileset(dir: "$siestaDir/bin") {
    include(name: '**')
  }
}

// Mark as prepared
ant.touch(file: preparedFile)
