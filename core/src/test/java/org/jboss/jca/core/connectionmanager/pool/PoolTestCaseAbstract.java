/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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
import org.jboss.jca.core.connectionmanager.rar.SimpleConnection;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionFactoryImpl1;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl;
import org.jboss.jca.core.connectionmanager.rar.SimpleConnectionImpl1;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory;
import org.jboss.jca.core.connectionmanager.rar.SimpleManagedConnectionFactory1;
import org.jboss.jca.core.connectionmanager.rar.SimpleResourceAdapter;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.ConnectionDefinitionsType;
import org.jboss.jca.embedded.dsl.ironjacamar11.api.IronjacamarDescriptor;

import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.resource.spi.ConnectionManager;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.connector15.ConnectorDescriptor;
import org.jboss.shrinkwrap.descriptor.api.connector15.OutboundResourceadapterType;
import org.jboss.shrinkwrap.descriptor.api.connector15.ResourceadapterType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * 
 * An PoolTestCaseAbstract.
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
   protected static Logger log = Logger.getLogger("PoolTestCaseAbstract");

   private  static final String jndiName = "java:/eis/Pool";

   /**
    * 
    * Creates default deployment with defined configurations
    * @param raXml ConnectionDescriptor
    * @param ijXml IronjacamarDescriptor
    * 
    * @return rar archive
    */
   public static ResourceAdapterArchive createDeployment(ConnectorDescriptor raXml, IronjacamarDescriptor ijXml)
   {
      ResourceAdapterArchive raa = ShrinkWrap.create(ResourceAdapterArchive.class, "pool.rar");
      JavaArchive ja = ShrinkWrap.create(JavaArchive.class);
      ja.addPackage(SimpleConnectionFactory.class.getPackage());
      raa.addAsLibrary(ja);
      raa.addAsManifestResource(new StringAsset(raXml.exportAsString()), "ra.xml");
      raa.addAsManifestResource(new StringAsset(ijXml.exportAsString()), "ironjacamar.xml");
      return raa;
   }

   /**
    * 
    * Create IronjacamarDescriptor
    * 
    * @param mcf  ManagedConnectionFactory class name
    * @return IronjacamarDescriptor
    */
   protected static IronjacamarDescriptor getBasicIJXml(String mcf)
   {
      IronjacamarDescriptor ijXml = Descriptors.create(IronjacamarDescriptor.class);
      ConnectionDefinitionsType ijCdst = ijXml.getOrCreateConnectionDefinitions();
      ConnectionDefinitionType ijCdt = ijCdst.createConnectionDefinition().className(mcf).jndiName(jndiName);
      return ijXml;
   }

   /**
    * 
    * create ConnectorDescriptor
    * 
    * @param tx Transaction support level
    * @return ConnectorDescriptor
    */
   protected static ConnectorDescriptor getRaXml(String tx)
   {
      ConnectorDescriptor raXml = Descriptors.create(ConnectorDescriptor.class, "ra.xml").version("1.5");
      ResourceadapterType rt = raXml.getOrCreateResourceadapter().resourceadapterClass(
         SimpleResourceAdapter.class.getName());

      OutboundResourceadapterType ort = rt.getOrCreateOutboundResourceadapter().transactionSupport(tx)
         .reauthenticationSupport(false);
      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt = ort.createConnectionDefinition()
         .managedconnectionfactoryClass(SimpleManagedConnectionFactory.class.getName())
         .connectionfactoryInterface(SimpleConnectionFactory.class.getName())
         .connectionfactoryImplClass(SimpleConnectionFactoryImpl.class.getName())
         .connectionInterface(SimpleConnection.class.getName())
         .connectionImplClass(SimpleConnectionImpl.class.getName());

      org.jboss.shrinkwrap.descriptor.api.connector15.ConnectionDefinitionType cdt1 = ort.createConnectionDefinition()
         .managedconnectionfactoryClass(SimpleManagedConnectionFactory1.class.getName())
         .connectionfactoryInterface(SimpleConnectionFactory.class.getName())
         .connectionfactoryImplClass(SimpleConnectionFactoryImpl1.class.getName())
         .connectionInterface(SimpleConnection.class.getName())
         .connectionImplClass(SimpleConnectionImpl1.class.getName());
      return raXml;
   }

   /**
    * 
    * create deployment of RA without transaction support
    * 
    * @param ijXml IronjacamarDescriptor
    * @return archive
    */
   public static ResourceAdapterArchive createNoTxDeployment(IronjacamarDescriptor ijXml)
   {
      return createDeployment(getRaXml("NoTransaction"), ijXml);
   }

   /**
    * connection factory
    */
   @Resource(mappedName = jndiName)
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

   /**
    * 
    * check pool behaviour
    * 
    * @throws Exception in case of error
    */
   @Test
   public void checkPool() throws Exception
   {
   }

}
