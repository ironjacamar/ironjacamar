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

package org.ironjacamar.rars.security;

import org.jboss.logging.Logger;

/**
 * UnifiedSecurityConnectionImpl
 *
 * @version $Revision: $
 */
public class UnifiedSecurityConnectionImpl implements UnifiedSecurityConnection
{
   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(UnifiedSecurityConnectionImpl.class.getName());

   /**
    * ManagedConnection
    */
   private UnifiedSecurityManagedConnection mc;

   /**
    * ManagedConnectionFactory
    */
   private UnifiedSecurityManagedConnectionFactory mcf;

   /**
    * Default constructor
    *
    * @param mc  UnifiedSecurityManagedConnection
    * @param mcf UnifiedSecurityManagedConnectionFactory
    */
   public UnifiedSecurityConnectionImpl(UnifiedSecurityManagedConnection mc,
         UnifiedSecurityManagedConnectionFactory mcf)
   {
      this.mc = mc;
      this.mcf = mcf;
   }

   /**
    * Close
    */
   public void close()
   {
      mc.closeHandle(this);
   }

   @Override public String getUserName()
   {

      return mc.getUserName();
   }

   @Override public String getPassword()
   {
      return mc.getPassword();
   }

   /**
    * {@inheritDoc}
    */
   @Override public void fail()
   {
      mc.errorHandle(this, new Exception());
   }

   /**
    *
    * set the managed connection
    * @param mc the managed connection
    */
   public void setManagedConnection(UnifiedSecurityManagedConnection mc)
   {
      this.mc = mc;
   }




}
