/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jca.deployers.test.unit.anno;

import org.jboss.jca.deployers.test.rars.anno.AnnoResourceAdapter;
import org.jboss.jca.deployers.test.rars.anno.variants.AnnoResourceAdapter1;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.Test;

/**
 * 
 * Proceeding annotations in resource adapter, containing 2 @Connector definitions
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class Wrong2ConnectorsTestCase extends AnnotationsTestBase
{

   @Override
   protected JavaArchive createArchive()
   {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
      jar.addClasses(AnnoResourceAdapter.class, AnnoResourceAdapter1.class);
      return jar;
   }

   @Override
   @Test(expected = Exception.class)
   public void proceed() throws Throwable
   {
      super.proceed();
   }
}
