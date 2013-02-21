/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.pool;

import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;

import java.util.logging.Logger;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.runner.RunWith;

/**
 * 
 * An AbstractPoolTestCase.
 * 
 * NOTE that this class is in org.jboss.jca.core.connectionmanager.pool and not in
 * org.jboss.jca.core.connectionmanager.pool.strategy because it needs to access 
 * to AbstractPool's package protected methods.
 * Please don't move it, and keep this class packaging consistent with AbstractPool's
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
@RunWith(Arquillian.class)
public abstract class PoolTestCaseAbstract
{

   /** The logger */
   protected static Logger log = Logger.getLogger("AbstractPoolTestCase");

   /**
    * 
    * Creates default deployment with different configurations
    * 
    * @param ijName ironjacamar.xml for deployment file name
    * @return rar archive
    */
   public static ResourceAdapterArchive getDeploymentWith(String ijName)
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "pool.rar");
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class);
      ja.addPackage(SimpleConnectionFactory.class.getPackage());
      raa.addAsLibrary(ja);
      raa.addAsManifestResource("rars/cm/META-INF/ra.xml", "ra.xml");
      raa.addAsManifestResource("rars/cm/META-INF/" + ijName, "ironjacamar.xml");
      return raa;
   }

   /**
    * 
    * Creates default deployment with ironjacamar.xml file
    * 
    * @return rar archive
    */
   public static ResourceAdapterArchive getDeployment()
   {
      return getDeploymentWith("ironjacamar.xml");
   }

   /**
    * 
    * To override
    * 
    * @return AbstractPool implementation
    */
   public abstract AbstractPool getPool();
}
