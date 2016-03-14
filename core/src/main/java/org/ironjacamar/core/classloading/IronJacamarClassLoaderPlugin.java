/*
 *  IronJacamar, a Java EE Connector Architecture implementation
 *  Copyright 2016, Red Hat Inc, and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the Eclipse Public License 1.0 as
 *  published by the Free Software Foundation.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 *  Public License for more details.
 *
 *  You should have received a copy of the Eclipse Public License
 *  along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.classloading;

import org.ironjacamar.core.spi.classloading.ClassLoaderPlugin;

/** The default inplementation of  SPI for class loading
 * This implementation is just using container classloading, ignoring any module parameters
 *
 * @author <a href="mailto:stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */

public class IronJacamarClassLoaderPlugin implements ClassLoaderPlugin
{
   @Override
   public Class<?> loadClass(String className, String moduleName, String moduleSlot) throws ClassNotFoundException
   {
      return SecurityActions.getClassLoader(IronJacamarClassLoaderPlugin.class).loadClass(className);
   }
}
