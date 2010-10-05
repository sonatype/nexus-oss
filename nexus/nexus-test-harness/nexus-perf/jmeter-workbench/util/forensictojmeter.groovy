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
