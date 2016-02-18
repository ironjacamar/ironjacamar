/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.ironjacamar.core.tx.noopts;

import org.ironjacamar.core.spi.security.SubjectFactory;
import org.ironjacamar.core.spi.transaction.recovery.XAResourceRecovery;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.transaction.xa.XAResource;

/**
 * An XAResourceRecovery implementation.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class XAResourceRecoveryImpl implements XAResourceRecovery
{
   /** The resource adapter */
   private ResourceAdapter rar;
   
   /** The ActivationSpec */
   private ActivationSpec as;
   
   /** The ManagedConnectionFactory */
   private ManagedConnectionFactory mcf;

   /** The ManagedConnection */
   private ManagedConnection mc;

   /** The security domain */
   private String securityDomain;

   /** The Subject factory */
   private SubjectFactory subjectFactory;

   /**
    * Constructor
    * @param rar The resource adapter
    * @param as The activation spec
    */
   public XAResourceRecoveryImpl(ResourceAdapter rar, ActivationSpec as)
   {
      this.rar = rar;
      this.as = as;
   }

   /**
    * Constructor
    * @param mcf The ManagedConnectionFactory
    * @param sd The security domain
    * @param subjectFactory The subject factory
    */
   public XAResourceRecoveryImpl(ManagedConnectionFactory mcf, String sd, SubjectFactory subjectFactory)
   {
      this.mcf = mcf;
      this.mc = null;
      this.securityDomain = sd;
      this.subjectFactory = subjectFactory;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize() throws Exception
   {
      if (rar != null)
      {
         for (XAResource xar : rar.getXAResources(new ActivationSpec[] {as}))
         {
            // Trigger a recovery pass
            xar.recover(XAResource.TMSTARTRSCAN);
            xar.recover(XAResource.TMENDRSCAN);
         }
      }
      else
      {
         // Create ManagedConnection
         mc = mcf.createManagedConnection(subjectFactory.createSubject(securityDomain), null);
         
         // Trigger a recovery pass
         mc.getXAResource().recover(XAResource.TMSTARTRSCAN);
         mc.getXAResource().recover(XAResource.TMENDRSCAN);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown() throws Exception
   {
      if (mc != null)
      {
         mc.cleanup();
         mc.destroy();
         mc = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public XAResource[] getXAResources()
   {
      return new XAResource[0];
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setJndiName(String jndiName)
   {
   }
}
