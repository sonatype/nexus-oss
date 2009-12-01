Nexus BUUP Plugin
=================

To be added:

* Two REST resources
  - one that will collect the info from user (memory sizing, etc), and start the process of download of the upgrade bundle (PUT) and show progress on that to UI (GET)?
  - one that will initiate the upgrade process (ie, user tracks the download progress, and when all set, will press a button)
  
* Nexus invoker part to Invoke BUUP is in place

* BuupInvokerPlugin will be responsible to (upon REST interaction) start the download of bundle, verify it, unzip it (to verify ZIP validity too)
* Also, this will show the "green light" to start the upgrade process (all the above is done succesfully)
* on next REST interaction, this component should call the invoker to invoke BUUP