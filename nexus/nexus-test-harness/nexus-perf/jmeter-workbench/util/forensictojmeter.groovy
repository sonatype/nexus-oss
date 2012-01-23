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
#!/usr/bin/env groovy

pathPrefix = "/nexus/content/groups/public"
if(this.args != null && this.args.length != 1){
    println "No log file to parse! "
    System.exit(1)
}

forensicLog = new File(this.args[0])
if(!forensicLog || !forensicLog.exists()){
    println "Log file does not exist (" + forensicLog.getAbsolutePath() +  ")"
    System.exit(1)
}

forensicLog.eachLine {
    line ->
    match = line.indexOf( pathPrefix );
    if(match == -1){
        return;
    }
    result = line.substring( match +
        + pathPrefix.length() );
    result = result.substring( 0, result.indexOf( ' ' ) );
    if(result != null && result.length() > 0){
        println(result)
    }
}
