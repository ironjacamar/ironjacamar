/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.ra10dtdout;

import org.jboss.jca.test.deployers.spec.rars.BaseCciConnectionFactory;
import org.jboss.jca.test.deployers.spec.rars.BaseConnectionManager;
import org.jboss.jca.test.deployers.spec.rars.BaseManagedConnectionFactory;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.jboss.logging.Logger;

/**
 * TestManagedConnectionFactory
 * @author <a href="mailto:jeff.zhang@jboss.org">Jeff Zhang</a>
 * @version $Revision: $
 */
public class TestManagedConnectionFactory extends BaseManagedConnectionFactory
{
   private static final long serialVersionUID = 1L;
   private static Logger log = Logger.getLogger(TestManagedConnectionFactory.class);
   
   /**
    * Config properties
    */
   private String aaa = "bbb";
   private Boolean bbb = true;

   /**
    * 
    * setter
    * 
    * @param value for aaa
    */
   public void setAaa(String value)
   {
      aaa = value;
   }

   /**
    * 
    * getter
    * 
    * @return aaa
    */
   public String getAaa()
   {
      return aaa;
   }

   /**
    * 
    * setter
    * 
    * @param value for bbb
    */
   public void setBbb(boolean value)
   {
      bbb = value;
   }

   /**
    * 
    * getter
    * 
    * @return bbb
    */
   public Boolean getBbb()
   {
      return bbb;
   }

   /**
    * Creates a Connection Factory instance. 
    *
    *  @param    cxManager    ConnectionManager to be associated with created EIS connection factory instance
    *  @return   EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    *  @throws   ResourceException     Generic exception
    */
   @Override
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      log.debug("call createConnectionFactory");
      return new BaseCciConnectionFactory(this);
   }

   /**
    * Creates a Connection Factory instance. 
    *
    *  @return   EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory instance
    *  @throws   ResourceException     Generic exception
    */
   @Override
   public Object createConnectionFactory() throws ResourceException
   {
      log.debug("call createConnectionFactory");
      return createConnectionFactory(new BaseConnectionManager());
   }
}
