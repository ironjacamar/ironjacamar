IronJacamar Eclipse plugin:
===========================

The IronJacamar Eclipse development plugin features development tools used
for developing resource adapter applications for the IronJacamar standalone distribution,
JBoss Application Server 7+ or JBoss Enterprise Application Platform 6+.

Installation:
-------------
cp ironjacamar-eclipse.jar $ECLIPSE_HOME/plugins

or

copy ironjacamar-eclipse.jar %ECLIPSE_HOME%\plugins

Configuration:
--------------
Open Window->Preferences and go to the IronJacamar category.

You must point the IronJacamar home setting to the root directory of the IronJacamar installation, like

 /opt/ironjacamar-1.1.0.Beta1

in order to configure the plugin.

New project:
------------
Select File->New->Project... and go to the IronJacamar category. 

Choose "IronJacamar 1.1 project" and follow the instructions to generate your resource adapter.
