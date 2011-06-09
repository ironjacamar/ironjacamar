/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.connectionmanager.connections.adapter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * ManagedConnectionFactory
 */
public class TestManagedConnectionFactory implements ManagedConnectionFactory
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Logger log = Logger.getLogger(TestManagedConnectionFactory.class); 
   
   private AtomicInteger id = new AtomicInteger(0);

   private String failure;

   private boolean failJoin;

   private long sleepInStart;

   private long sleepInEnd;
   
   private Map<GlobalXID, String> xids = new HashMap<GlobalXID, String>();

   /**
    * Constructor
    */
   public TestManagedConnectionFactory()
   {
   }

   /**
    * Set failure
    * @param failure failure
    */
   public void setFailure(String failure)
   {
      this.failure = failure;
   }

   /**
    * Get fail join
    * @return The value
    */
   public boolean getFailJoin()
   {
      return failJoin;
   }
   
   /**
    * Set fail join
    * @param failJoin The value
    */
   public void setFailJoin(boolean failJoin)
   {
      this.failJoin = failJoin;
   }

   /**
    * Get sleep in start
    * @return The value
    */
   public long getSleepInStart()
   {
      return sleepInStart;
   }
   
   /**
    * Set sleep in start
    * @param sleep The value
    */
   public void setSleepInStart(long sleep)
   {
      this.sleepInStart = sleep;
   }

   /**
    * Get sleep in end
    * @return The value
    */
   public long getSleepInEnd()
   {
      return sleepInEnd;
   }
   
   /**
    * Set sleep in end
    * @param sleep The value
    */
   public void setSleepInEnd(long sleep)
   {
      this.sleepInEnd = sleep;
   }
   
   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory(ConnectionManager cm) throws ResourceException
   {
      return new TestConnectionFactory(cm, this);
   }

   /**
    * {@inheritDoc}
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("NYI");
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      if (failure != null && failure.equals("createManagedConnectionResource"))
         throw new ResourceException("");

      if (failure != null && failure.equals("createManagedConnectionRuntime"))
         throw new RuntimeException("");

      return new TestManagedConnection(this, id.incrementAndGet());
   }

   /**
    * {@inheritDoc}
    */
   public ManagedConnection matchManagedConnections(Set candidates, Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      if (failure != null && failure.equals("matchManagedConnectionResource"))
         throw new ResourceException("");

      if (failure != null && failure.equals("matchManagedConnectionRuntime"))
         throw new RuntimeException("");

      if (candidates.isEmpty()) 
         return null;

      return (ManagedConnection)candidates.iterator().next();
   }

   /**
    * Get the Xids
    * @return The value
    */
   Map<GlobalXID, String> getXids()
   {
      return xids;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      return getClass().hashCode();
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object other)
   {
      return (other != null) && (other.getClass() == getClass());
   }
}
