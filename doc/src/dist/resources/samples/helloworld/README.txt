The HelloWorld resource adapter
===============================

Introduction
------------
The HelloWorld resource adapter example shows a simple example of how to use and
implement the interfaces in the Java EE Connector Architecture specification.

The HelloWorld examples exposes the HelloWorldConnection interface where developers
can invoke the exposed methods.

The example shows how to build and test a resource adapter.

How to build and test
---------------------
1. Install Ant 1.8 (http://ant.apache.org)

2. Copy all libs from lib/ directory

    cd doc/samples/helloworld
    cp -R ../../../lib .

   Note that the above refers to the standalone distribution of IronJacamar.

3. Build

    ant

4. Test

    ant test
