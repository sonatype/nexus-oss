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
