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
package org.jboss.jca.core.tx.jbossts;

import org.jboss.jca.core.CoreLogger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * An XAResourceRecovery for inflow implementation.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XAResourceRecoveryInflowImpl implements org.jboss.jca.core.spi.transaction.recovery.XAResourceRecovery,
                                                     org.jboss.tm.XAResourceRecovery
{
   /** Log instance */
   private static CoreLogger log = Logger.getMessageLogger(CoreLogger.class,
                                                           XAResourceRecoveryInflowImpl.class.getName());

   private ResourceAdapter resourceAdapter;

   private ActivationSpec activationSpec;

   private String productName;

   private String productVersion;

   private String jndiName;

   /**
    * Constructor
    *
    * @param rar The resource adapter
    * @param as The activation spec
    * @param productName The product name
    * @param productVersion The product version
    */
   public XAResourceRecoveryInflowImpl(ResourceAdapter rar, ActivationSpec as,
                                       String productName, String productVersion)
   {
      if (rar == null)
         throw new IllegalArgumentException("ResourceAdapter is null");

      if (as == null)
         throw new IllegalArgumentException("ActivationSpec is null");

      this.resourceAdapter = rar;
      this.activationSpec = as;
      this.productName = productName;
      this.productVersion = productVersion;
      this.jndiName = null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize() throws Exception
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown() throws Exception
   {
   }

   /**
    * {@inheritDoc}
    */
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public XAResource[] getXAResources()
   {
      try
      {
         XAResource[] result = resourceAdapter.getXAResources(new ActivationSpec[] {activationSpec});

         if (result == null || result.length == 0)
            return result;

         XAResource[] newResult = new XAResource[result.length];
         for (int i = 0; i < result.length; i++)
         {
            newResult[i] = new XAResourceWrapperImpl(result[i], productName, productVersion);
         }
         return newResult;
      }
      catch (NotSupportedException nse)
      {
         // Ignore
      }
      catch (ResourceException re)
      {
         log.exceptionDuringCrashRecoveryInflow(resourceAdapter.getClass().getName(), activationSpec, re);
      }

      return new XAResource[0];
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      sb.append("XAResourceRecoveryInflowImpl@").append(Integer.toHexString(System.identityHashCode(this)));
      sb.append("[");
      sb.append(" rar=").append(resourceAdapter.getClass().getName());
      sb.append(" as=").append(activationSpec);
      sb.append("]");

      return sb.toString();
   }
}
