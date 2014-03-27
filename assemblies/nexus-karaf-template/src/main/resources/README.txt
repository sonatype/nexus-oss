First boot Karaf framework with:

   ./nexus-karaf-template-3.0.0-SNAPSHOT/karaf/bin/karaf

Start Nexus from inside Karaf with:

   karaf@root()>  nexus-start

Stop Nexus from inside Karaf with:

   karaf@root()>  nexus-stop

This will stop and uninstall the extra plugins/bundles installed by Nexus to get back to a clean state.

Once we have Karaf features for Nexus and each plugin these commands won't be needed anymore.

