First boot Karaf framework with:

   ./nexus-karaf-template-3.0.0-SNAPSHOT/karaf/bin/karaf

Make sure that Karaf has started with the basic bootstrap bundles/fragments:

   karaf@root()>  list

   START LEVEL 100 , List Threshold: 50
   ID | State     | Lvl | Version          | Name                                
   ------------------------------------------------------------------------------
   88 | Installed |  80 | 3.0.0.SNAPSHOT   | org.sonatype.nexus:nexus-oss-edition
   89 | Installed |  80 | 3.0.0.SNAPSHOT   | org.sonatype.nexus:nexus-plugin-api 
   90 | Installed |  80 | 8.1.11.v20130520 | Jetty OSGi Boot                     
   91 | Resolved  |  80 | 1.48             | bcprov                              

Start Nexus from inside Karaf with:

   karaf@root()>  nexus-start

Note: starting the plugins takes longer on Karaf than on pure-Felix (used in the webapp) at the moment.

Once Nexus is fully initialized, point your browser to:

   http://127.0.0.1:8080/nexus/

Stop Nexus from inside Karaf with:

   karaf@root()>  nexus-stop

This will stop and uninstall the extra plugins/bundles installed by Nexus to get back to a clean state.

Once we have Karaf features for Nexus and each plugin these commands won't be needed anymore.

