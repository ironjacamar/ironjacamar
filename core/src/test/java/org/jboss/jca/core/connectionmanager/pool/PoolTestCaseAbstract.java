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

import org.jboss.jca.core.api.connectionmanager.pool.PoolStatistics;
import org.jboss.jca.core.connectionmanager.ConnectionManagerUtil;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.resource.spi.ConnectionManager;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.runner.RunWith;

import static org.junit.Assert.*;

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
    * connection factory
    */
   @Resource(mappedName = "java:/eis/Pool")
   SimpleConnectionFactory cf;

   /**
    * 
    * get Pool from CF
    * 
    * @return AbstractPool implementation
    */
   public AbstractPool getPool()
   {
      return (AbstractPool) ConnectionManagerUtil.extract(cf).getPool();
   }

   /**
    * 
    * Checks statistics
    * 
    * @param ps PoolStatistics implementation
    * @param available count
    * @param inUse count
    * @param active count
    */
   public void checkStatistics(PoolStatistics ps, int available, int inUse, int active)
   {
      log.info("/// Statistics of " + ps.getClass() + ": " + ps);
      assertEquals("ActiveCount value is " + ps.getActiveCount() + " but expected value is " + active,
         ps.getActiveCount(), active);
      assertEquals("InUseCount value is " + ps.getInUseCount() + " but expected value is " + inUse, ps.getInUseCount(),
         inUse);
      assertEquals("AvailableCount value is " + ps.getAvailableCount() + " but expected value is " + available,
         ps.getAvailableCount(), available);
   }

   /**
    * 
    * Checks statistics
    * 
    * @param ps PoolStatistics implementation
    * 
    * @param available count
    * @param inUse count
    * @param active count
    * @param destroyed count
    */
   public void checkStatistics(PoolStatistics ps, int available, int inUse, int active, int destroyed)
   {
      checkStatistics(ps, available, inUse, active);
      assertEquals("DestroyedCount value is " + ps.getDestroyedCount() + " but expected value is " + destroyed,
         ps.getDestroyedCount(), destroyed);
   }

   /**
    * 
    * checkConfiguration
    * @param cmClass class, implementing ConnectionManager in configuration
    * @param poolClass class, implementing Pool in configuration
    * 
    */
   public void checkConfiguration(Class<? extends ConnectionManager> cmClass, Class<? extends AbstractPool> poolClass)
   {
      assertTrue("ConnectionFactory " + cf + " should contain this ConnectionManager implementation: " + cmClass +
                 " but got " + ConnectionManagerUtil.extract(cf).getClass(),
         cmClass.isAssignableFrom(ConnectionManagerUtil.extract(cf).getClass()));
      AbstractPool pool = getPool();
      assertTrue("There should be a " + poolClass + " implementation of Pool, but got " + pool.getClass(),
         poolClass.isAssignableFrom(pool.getClass()));
      assertEquals("Pool's MCF should be " + cf.getMCF() + " but got " + pool.getManagedConnectionFactory(),
         pool.getManagedConnectionFactory(), cf.getMCF());
   }
}
