# IronJacamar

IronJacamar implements the Jakarta Connectors 2.1 specification.

IronJacamar is licensed under [GNU LESSER GENERAL PUBLIC LICENSE 2.1 (LGPL 2.1)](http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html "LGPL v2.1")

[http://www.ironjacamar.org/](http://www.ironjacamar.org/ "IronJacamar homepage")

## Jakarta Connectors

The Jakarta Connectors defines a standard architecture for connecting the Jakarta EE platform to heterogeneous Enterprise Information Systems (EIS). Examples of EISs include Enterprise Resource Planning (ERP), mainframe transaction processing (TP), databases and messaging systems.

The connector architecture defines a set of scalable, secure, and transactional mechanisms that enable the integration of EISs with application servers and enterprise applications.

The connector architecture also defines a Common Client Interface (CCI) for EIS access. The CCI defines a client API for interacting with heterogeneous EISs.

The connector architecture enables an EIS vendor to provide a standard resource adapter for its EIS. A resource adapter is a system-level software driver that is used by a Java application to connect to an EIS. The resource adapter plugs into an application server and provides connectivity between the EIS, the application server, and the enterprise application. The resource adapter serves as a protocol adapter that allows any arbitrary EIS communication protocol to be used for connectivity. An application server vendor extends its system once to support the connector architecture and is then assured of seamless connectivity to multiple EISs. Likewise, an EIS vendor provides one standard resource adapter which has the capability to plug in to any application server that supports the connector architecture.

[Jakarta Connectors 2.1 Specification](https://jakarta.ee/specifications/connectors/2.1/jakarta-connectors-spec-2.1.html)

## Build with Maven

The command below builds the project and runs the embedded suite.

```console
$ mvn clean install
```

## Issue Tracking

Bugs and features are tracked within the IronJacamar Jira project at https://issues.jboss.org/browse/JBJCA

## Contributions

All new features and enhancements should be submitted to _main_ branch.
Our [contribution guide](https://github.com/ironjacamar/ironjacamar/blob/main/CONTRIBUTING.md) will guide you through the steps for getting started on the IronJacamar project and will go through how to format and submit your first PR.


## Get Help

If you would like to ask us some question or you need some help, feel free to ask on the WildFly user [forum](https://groups.google.com/g/wildfly).