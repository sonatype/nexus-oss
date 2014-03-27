First boot Karaf framework with:

   ./nexus-karaf-template-3.0.0-SNAPSHOT/karaf/bin/karaf

Start Nexus from inside Karaf with:

   karaf@root()>  nexus-start

Note: starting the plugins takes longer on Karaf than on pure-Felix (used in the webapp) at the moment.

Once Nexus is fully initialized, point your browser to:

   http://127.0.0.1:8080/nexus/

Stop Nexus from inside Karaf with:

   karaf@root()>  nexus-stop

This will stop and uninstall the extra plugins/bundles installed by Nexus to get back to a clean state.

Once we have Karaf features for Nexus and each plugin these commands won't be needed anymore.

