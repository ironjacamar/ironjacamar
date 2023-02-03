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
package org.jboss.jca.deployers.test.rars.stat;

import org.jboss.jca.core.spi.statistics.Statistics;
import org.jboss.jca.core.spi.statistics.StatisticsPlugin;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 * StatisticsManagedConnectionFactory
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class StatisticsManagedConnectionFactory implements ManagedConnectionFactory,
                                                           ResourceAdapterAssociation,
                                                           Statistics
{
   /** The serial version UID */
   private static final long serialVersionUID = 1L;

   /** The logger */
   private static Logger log = Logger.getLogger(StatisticsManagedConnectionFactory.class.getName());

   /** The resource adapter */
   private ResourceAdapter ra;

   /** The logwriter */
   private PrintWriter logwriter;

   /** enableStat */
   private Boolean enableStat;

   /** Statistics plugin */
   private StatisticsPlugin statPlugin;

   /**
    * Default constructor
    */
   public StatisticsManagedConnectionFactory()
   {
      statPlugin = new StatisticsManagedConnectionFactoryStatisticsPlugin();
   }

   /** 
    * Set enableStat
    * @param enableStat The value
    */
   public void setEnableStat(Boolean enableStat)
   {
      this.enableStat = enableStat;
   }

   /** 
    * Get enableStat
    * @return The value
    */
   public Boolean getEnableStat()
   {
      return enableStat;
   }

   /**
    * Creates a Connection Factory instance. 
    *
    * @param cxManager ConnectionManager to be associated with created EIS connection factory instance
    * @return EIS-specific Connection Factory instance or jakarta.resource.cci.ConnectionFactory instance
    * @throws ResourceException Generic exception
    */
   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      log.tracef("createConnectionFactory(%s)", cxManager);
      return new StatisticsConnectionFactoryImpl(this, cxManager);
   }

   /**
    * Creates a Connection Factory instance. 
    *
    * @return EIS-specific Connection Factory instance or jakarta.resource.cci.ConnectionFactory instance
    * @throws ResourceException Generic exception
    */
   public Object createConnectionFactory() throws ResourceException
   {
      throw new ResourceException("This resource adapter doesn't support non-managed environments");
   }

   /**
    * Creates a new physical connection to the underlying EIS resource manager.
    *
    * @param subject Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @throws ResourceException generic exception
    * @return ManagedConnection instance 
    */
   public ManagedConnection createManagedConnection(Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.tracef("createManagedConnection(%s, %s)", subject, cxRequestInfo);
      return new StatisticsManagedConnection(this);
   }

   /**
    * Returns a matched connection from the candidate set of connections. 
    *
    * @param connectionSet Candidate connection set
    * @param subject Caller's security information
    * @param cxRequestInfo Additional resource adapter specific connection request information
    * @throws ResourceException generic exception
    * @return ManagedConnection if resource adapter finds an acceptable match otherwise null 
    */
   public ManagedConnection matchManagedConnections(Set connectionSet,
         Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      log.tracef("matchManagedConnections(%s, %s, %s)", connectionSet, subject, cxRequestInfo);
      ManagedConnection result = null;
      Iterator it = connectionSet.iterator();
      while (result == null && it.hasNext())
      {
         ManagedConnection mc = (ManagedConnection)it.next();
         if (mc instanceof StatisticsManagedConnection)
         {
            result = mc;
         }

      }
      return result;
   }

   /**
    * Get the log writer for this ManagedConnectionFactory instance.
    *
    * @return PrintWriter
    * @throws ResourceException generic exception
    */
   public PrintWriter getLogWriter() throws ResourceException
   {
      log.trace("getLogWriter()");
      return logwriter;
   }

   /**
    * Set the log writer for this ManagedConnectionFactory instance.
    *
    * @param out PrintWriter - an out stream for error logging and tracing
    * @throws ResourceException generic exception
    */
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
      log.tracef("setLogWriter(%s)", out);
      logwriter = out;
   }

   /**
    * Get the resource adapter
    *
    * @return The handle
    */
   public ResourceAdapter getResourceAdapter()
   {
      log.trace("getResourceAdapter()");
      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      log.tracef("setResourceAdapter(%s)", ra);
      this.ra = ra;
   }

   /**
    * Get the statistics plugin
    * @return The value
    */
   public StatisticsPlugin getStatistics()
   {
      return statPlugin;
   }

   /** 
    * Returns a hash code value for the object.
    * @return A hash code value for this object.
    */
   @Override
   public int hashCode()
   {
      int result = 17;
      if (enableStat != null)
         result += 31 * result + 7 * enableStat.hashCode();
      else
         result += 31 * result + 7;
      return result;
   }

   /** 
    * Indicates whether some other object is equal to this one.
    * @param other The reference object with which to compare.
    * @return true if this object is the same as the obj argument, false otherwise.
    */
   @Override
   public boolean equals(Object other)
   {
      if (other == null)
         return false;
      if (other == this)
         return true;
      if (!(other instanceof StatisticsManagedConnectionFactory))
         return false;
      boolean result = true;
      StatisticsManagedConnectionFactory obj = (StatisticsManagedConnectionFactory)other;
      if (result)
      {
         if (enableStat == null)
            result = obj.getEnableStat() == null;
         else
            result = enableStat.equals(obj.getEnableStat());
      }
      return result;
   }

}
