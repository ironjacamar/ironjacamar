IronJacamar
===========

IronJacamar implements the Java EE Connector Architecture 1.7 specification,
and is certified in both the standalone profile, and full profile.

IronJacamar is licensed under [GNU LESSER GENERAL PUBLIC LICENSE 2.1 (LGPL 2.1)](http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html "LGPL v2.1")

[http://www.ironjacamar.org/](http://www.ironjacamar.org/ "IronJacamar homepage")

Contributing
------------

* Write a blog about IronJacamar
* Create a tutorial for IronJacamar
* Help expand the [user guide](http://www.ironjacamar.org/doc/userguide/1.2/en-US/html/index.html)
* Answer questions and share ideas on the [IRC channel](http://webchat.freenode.net/?channels=ironjacamar)
* Test [releases](http://www.ironjacamar.org/download.html)
* Review [pull requests](http://github.com/ironjacamar/ironjacamar/pulls)
* Start [hacking on IronJacamar](http://www.ironjacamar.org/doc/developerguide/1.1/en-US/html/ch02.html)

Building
--------

### Bundle

            ant clean sjc

### Release

            ant -Declipse.home=/opt/eclipse clean release
