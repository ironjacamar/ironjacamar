The HelloWorld/Native resource adapter
======================================

Introduction
------------
The HelloWorld/Native resource adapter sample shows a simple example of how to use and
implement the interfaces in the Java EE Connector Architecture specification which calls a
native library.

The HelloWorld/Native sample exposes the HelloWorldConnection interface where developers
can invoke the exposed methods.

The sample shows how to build and test a resource adapter.

How to build and test
---------------------
1. Install Ant 1.8 (http://ant.apache.org)

2. Copy all libs from sjc/lib directory

    cd doc/samples/helloworld-native
    cp -R ../../../lib .
    cp ../../../bin/ironjacamar-sjc.jar lib/

   Note that the above refers to the standalone distribution of IronJacamar.

3. Build

    ant native
    cmake .
    make
    ant rar

4. Test

    ant test
