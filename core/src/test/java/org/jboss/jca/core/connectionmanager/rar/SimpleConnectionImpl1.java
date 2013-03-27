/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.rar;

import java.util.logging.Logger;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * SimpleConnectionImpl
 *
 * @version $Revision: $
 */
public class SimpleConnectionImpl1 implements SimpleConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger("SimpleConnectionImpl1");

   /** ManagedConnection */
   private SimpleManagedConnection1 mc;

   /** ManagedConnectionFactory */
   private SimpleManagedConnectionFactory1 mcf;

   /** ConnectionRequestInfo */
   private ConnectionRequestInfo cri;

   /**
    * Default constructor
    * @param mc SimpleManagedConnection
    * @param mcf SimpleManagedConnectionFactory
    * @param cri ConnectionRequestInfo
    */
   public SimpleConnectionImpl1(SimpleManagedConnection1 mc, SimpleManagedConnectionFactory1 mcf,
      ConnectionRequestInfo cri)
   {
      log.info("Constructor");
      this.mc = mc;
      this.mcf = mcf;
      if (cri == null || cri instanceof SimpleConnectionRequestInfoImpl)
         this.cri = cri;
      else
         throw new RuntimeException("CRI of wrong type:" + cri);

   }

   @Override
   public void callMe()
   {
      mc.callMe();
   }

   @Override
   public void close()
   {
      mc.closeHandle(this);
   }

   @Override
   public void fail()
   {
      mc.failHandle(this);
   }

   /**
    * 
    * getter
    * 
    * @return cri cri
    */
   public ConnectionRequestInfo getCri()
   {
      return cri;
   }

}
