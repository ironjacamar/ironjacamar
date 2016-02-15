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
package org.ironjacamar.rars.test;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.RetryableUnavailableException;
import javax.resource.spi.ValidatingManagedConnectionFactory;

import javax.security.auth.Subject;

/**
 * TestManagedConnectionFactory
 */
public class TestManagedConnectionFactory implements ManagedConnectionFactory,
                                                     ResourceAdapterAssociation,
                                                     ValidatingManagedConnectionFactory
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The resource adapter */
   private ResourceAdapter ra;

   /** The logwriter */
   private PrintWriter logwriter;

   /** Create failure count */
   private Integer createFailureCount;

   /** Invalid connection failure count */
   private Integer invalidConnectionFailureCount;

   /**
    * Default constructor
    */
   public TestManagedConnectionFactory()
   {
      this.createFailureCount = Integer.valueOf(0);
      this.invalidConnectionFailureCount = Integer.valueOf(0);
   }

   /** 
    * Set create failure count
    * @param v The value
    */
   public void setCreateFailureCount(Integer v)
   {
      this.createFailureCount = v;
   }

   /** 
    * Get create failure count
    * @return The value
    */
   public Integer getCreateFailureCount()
   {
      return createFailureCount;
   }

   /** 
    * Set invalid connection failure count
    * @param v The value
    */
   public void setInvalidConnectionFailureCount(Integer v)
   {
      this.invalidConnectionFailureCount = v;
   }

   /** 
    * Get invalid connection failure count
    * @return The value
    */
   public Integer getInvalidConnectionFailureCount()
   {
      return invalidConnectionFailureCount;
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return new TestConnectionFactoryImpl(this, cxManager);
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("This resource adapter doesn't support non-managed environments");
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection createManagedConnection(Subject subject,
                                                    ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      if (createFailureCount.intValue() > 0)
      {
         createFailureCount--;
         throw new RetryableUnavailableException();
      }
      
      return new TestManagedConnection(this);
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection matchManagedConnections(Set connectionSet,
                                                    Subject subject,
                                                    ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      ManagedConnection result = null;
      Iterator it = connectionSet.iterator();
      while (result == null && it.hasNext())
      {
         ManagedConnection mc = (ManagedConnection)it.next();
         if (mc instanceof TestManagedConnection)
         {
            result = mc;
         }

      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public Set getInvalidConnections(Set connectionSet) throws ResourceException
   {
      Set result = new HashSet();

      Iterator it = connectionSet.iterator();
      while (invalidConnectionFailureCount > 0 && it.hasNext())
      {
         result.add(it.next());
         invalidConnectionFailureCount--;
      }
      
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return logwriter;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      logwriter = out;
   }

   /**
    * {@inheritDoc}
    */
   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   /**
    * {@inheritDoc}
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      this.ra = ra;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      if (invalidConnectionFailureCount != null)
         result += 31 * result + 7 * invalidConnectionFailureCount.hashCode();
      else
         result += 31 * result + 7;
      if (createFailureCount != null)
         result += 31 * result + 7 * createFailureCount.hashCode();
      else
         result += 31 * result + 7;
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;
      if (other == this)
         return true;
      if (!(other instanceof TestManagedConnectionFactory))
         return false;
      boolean result = true;
      TestManagedConnectionFactory obj = (TestManagedConnectionFactory)other;
      if (result)
      {
         if (invalidConnectionFailureCount == null)
            result = obj.getInvalidConnectionFailureCount() == null;
         else
            result = invalidConnectionFailureCount.equals(obj.getInvalidConnectionFailureCount());
      }
      if (result)
      {
         if (createFailureCount == null)
            result = obj.getCreateFailureCount() == null;
         else
            result = createFailureCount.equals(obj.getCreateFailureCount());
      }
      return result;
   }
}
