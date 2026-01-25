The HelloWorld/Lazy resource adapter
====================================

Introduction
------------
The HelloWorld/Lazy resource adapter example shows a simple example of how to use and
implement the interfaces in the Java EE Connector Architecture specification which
takes advantage of the lazy association mechanism to reestablish the relationship
between the logical (HelloWorldConnectionImpl) and the physical connection
(HelloWorldManagedConnection).

The HelloWorld/Lazy sample exposes the HelloWorldConnection interface where developers
can invoke the exposed methods.

The example shows how to build and test a resource adapter.

How to build and test
---------------------
1. Install Ant 1.8 (http://ant.apache.org)

2. Copy all libs from sjc/lib directory

    cd doc/samples/helloworld-lazy
    cp -R ../../../lib .
    cp ../../../bin/ironjacamar-sjc.jar lib/

   Note that the above refers to the standalone distribution of IronJacamar.

3. Build

    ant

4. Test

    ant test
