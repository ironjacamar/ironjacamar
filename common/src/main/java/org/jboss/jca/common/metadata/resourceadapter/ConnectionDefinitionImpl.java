/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.Security;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.common.XaPool;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A ConnectionDefinition.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ConnectionDefinitionImpl implements ConnectionDefinition
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** config-property */
   protected Map<String, String> configProperties;

   /** class-name */
   protected String className;

   /** jndi-name */
   protected String jndiName;

   /** pool-name */
   protected String poolName;

   /** enable */
   protected Boolean enabled;

   /** use-java-context */
   protected Boolean useJavaContext;

   /** use-ccm */
   protected Boolean useCcm;

   /** pool */
   protected Pool pool;

   /** timeout */
   protected TimeOut timeOut;

   /** validation */
   protected Validation validation;

   /** security */
   protected Security security;

   /** recovery */
   protected Recovery recovery;

   /** isXA */
   protected Boolean isXA;

   /** Sharable */
   protected Boolean sharable;

   /** Enlistment */
   protected Boolean enlistment;

   private Boolean connectable;

   private Boolean tracking;

   private String mcp;

   private Boolean enlistmentTrace;

   /**
    * Create a new ConnectionDefinition.
    *
    * @param configProperties configProperties
    * @param className className
    * @param jndiName jndiName
    * @param poolName poolName
    * @param enabled enabled
    * @param useJavaContext useJavaContext
    * @param useCcm useCcm
    * @param sharable sharable
    * @param enlistment enlistment
    * @param connectable connectable
    * @param tracking tracking
    * @param mcp mcp
    * @param enlistmentTrace enlistmentTrace
    * @param pool pool
    * @param timeOut timeOut
    * @param validation validation
    * @param security security
    * @param recovery recovery
    * @param isXA isXA
    */
   public ConnectionDefinitionImpl(Map<String, String> configProperties, String className, String jndiName,
                                   String poolName, Boolean enabled, Boolean useJavaContext, Boolean useCcm,
                                   Boolean sharable, Boolean enlistment, Boolean connectable, Boolean tracking,
                                   String mcp, Boolean enlistmentTrace, Pool pool, TimeOut timeOut,
                                   Validation validation, Security security, Recovery recovery, Boolean isXA)
   {
      if (configProperties != null)
      {
         this.configProperties = new HashMap<String, String>(configProperties.size());
         this.configProperties.putAll(configProperties);
      }
      else
      {
         this.configProperties = new HashMap<String, String>(0);
      }
      this.className = className;
      this.jndiName = jndiName;
      this.poolName = poolName;
      this.enabled = enabled;
      this.useJavaContext = useJavaContext;
      this.useCcm = useCcm;
      this.pool = pool;
      this.timeOut = timeOut;
      this.validation = validation;
      this.security = security;
      this.recovery = recovery;
      this.isXA = isXA;

      this.sharable = sharable;
      this.enlistment = enlistment;

      this.connectable = connectable;
      this.tracking = tracking;

      this.mcp = mcp;
      this.enlistmentTrace = enlistmentTrace;
   }

   /**
    * {@inheritDoc}
    */
   public Map<String, String> getConfigProperties()
   {
      return Collections.unmodifiableMap(configProperties);
   }

   /**
    * {@inheritDoc}
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * {@inheritDoc}
    */
   public String getJndiName()
   {
      return jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public String getPoolName()
   {
      return poolName;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isEnabled()
   {
      return enabled;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isUseJavaContext()
   {
      return useJavaContext;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isUseCcm()
   {
      return useCcm;
   }

   /**
    * {@inheritDoc}
    */
   public Pool getPool()
   {
      return pool;
   }

   /**
    * {@inheritDoc}
    */
   public TimeOut getTimeOut()
   {
      return timeOut;
   }

   /**
    * {@inheritDoc}
    */
   public Validation getValidation()
   {
      return validation;
   }

   /**
    * {@inheritDoc}
    */
   public Security getSecurity()
   {
      return security;
   }

   /**
    * {@inheritDoc}
    */
   public Recovery getRecovery()
   {
      return recovery;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isXa()
   {
      return (pool instanceof XaPool) || isXA != null ? isXA : Boolean.FALSE;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isSharable()
   {
      return sharable;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isEnlistment()
   {
      return enlistment;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isConnectable()
   {
      return connectable;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isTracking()
   {
      return tracking;
   }

   /**
    * {@inheritDoc}
    */
   public String getMcp()
   {
      return mcp;
   }

   /**
    * {@inheritDoc}
    */
   public Boolean isEnlistmentTrace()
   {
      return enlistmentTrace;
   }

   /**
    * {@inheritDoc}
    */
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((configProperties == null) ? 0 : configProperties.hashCode());
      result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
      result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
      result = prime * result + ((pool == null) ? 0 : pool.hashCode());
      result = prime * result + ((poolName == null) ? 0 : poolName.hashCode());
      result = prime * result + ((recovery == null) ? 0 : recovery.hashCode());
      result = prime * result + ((security == null) ? 0 : security.hashCode());
      result = prime * result + ((timeOut == null) ? 0 : timeOut.hashCode());
      result = prime * result + ((useJavaContext == null) ? 0 : useJavaContext.hashCode());
      result = prime * result + ((useCcm == null) ? 0 : useCcm.hashCode());
      result = prime * result + ((validation == null) ? 0 : validation.hashCode());
      result = prime * result + ((isXA == null) ? 0 : isXA.hashCode());

      result = prime * result + ((sharable == null) ? 0 : sharable.hashCode());
      result = prime * result + ((enlistment == null) ? 0 : enlistment.hashCode());

      result = prime * result + ((connectable == null) ? 0 : connectable.hashCode());
      result = prime * result + ((tracking == null) ? 0 : tracking.hashCode());

      result = prime * result + ((mcp == null) ? 0 : mcp.hashCode());
      result = prime * result + ((enlistmentTrace == null) ? 0 : enlistmentTrace.hashCode());

      return result;
   }

   /**
    * {@inheritDoc}
    */
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof ConnectionDefinitionImpl))
         return false;

      ConnectionDefinitionImpl other = (ConnectionDefinitionImpl) obj;
      if (className == null)
      {
         if (other.className != null)
            return false;
      }
      else if (!className.equals(other.className))
         return false;
      if (configProperties == null)
      {
         if (other.configProperties != null)
            return false;
      }
      else if (!configProperties.equals(other.configProperties))
         return false;
      if (enabled == null)
      {
         if (other.enabled != null)
            return false;
      }
      else if (!enabled.equals(other.enabled))
         return false;
      if (jndiName == null)
      {
         if (other.jndiName != null)
            return false;
      }
      else if (!jndiName.equals(other.jndiName))
         return false;
      if (pool == null)
      {
         if (other.pool != null)
            return false;
      }
      else if (!pool.equals(other.pool))
         return false;
      if (poolName == null)
      {
         if (other.poolName != null)
            return false;
      }
      else if (!poolName.equals(other.poolName))
         return false;
      if (recovery == null)
      {
         if (other.recovery != null)
            return false;
      }
      else if (!recovery.equals(other.recovery))
         return false;
      if (security == null)
      {
         if (other.security != null)
            return false;
      }
      else if (!security.equals(other.security))
         return false;
      if (timeOut == null)
      {
         if (other.timeOut != null)
            return false;
      }
      else if (!timeOut.equals(other.timeOut))
         return false;
      if (useJavaContext == null)
      {
         if (other.useJavaContext != null)
            return false;
      }
      else if (!useJavaContext.equals(other.useJavaContext))
         return false;
      if (useCcm == null)
      {
         if (other.useCcm != null)
            return false;
      }
      else if (!useCcm.equals(other.useCcm))
         return false;
      if (validation == null)
      {
         if (other.validation != null)
            return false;
      }
      else if (!validation.equals(other.validation))
         return false;
      if (isXA == null)
      {
         if (other.isXA != null)
            return false;
      }
      else if (!isXA.equals(other.isXA))
         return false;

      if (sharable == null)
      {
         if (other.sharable != null)
            return false;
      }
      else if (!sharable.equals(other.sharable))
         return false;
      if (enlistment == null)
      {
         if (other.enlistment != null)
            return false;
      }
      else if (!enlistment.equals(other.enlistment))
         return false;

      if (connectable == null)
      {
         if (other.connectable != null)
            return false;
      }
      else if (!connectable.equals(other.connectable))
         return false;
      if (tracking == null)
      {
         if (other.tracking != null)
            return false;
      }
      else if (!tracking.equals(other.tracking))
         return false;
      if (mcp == null)
      {
         if (other.mcp != null)
            return false;
      }
      else if (!mcp.equals(other.mcp))
         return false;
      if (enlistmentTrace == null)
      {
         if (other.enlistmentTrace != null)
            return false;
      }
      else if (!enlistmentTrace.equals(other.enlistmentTrace))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<connection-definition");

      if (className != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.CLASS_NAME).append("=\"").append(className).append("\"");

      if (jndiName != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.JNDI_NAME).append("=\"").append(jndiName).append("\"");

      if (enabled != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.ENABLED).append("=\"").append(enabled).append("\"");

      if (useJavaContext != null)
      {
         sb.append(" ").append(ConnectionDefinition.Attribute.USE_JAVA_CONTEXT);
         sb.append("=\"").append(useJavaContext).append("\"");
      }

      if (poolName != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.POOL_NAME).append("=\"").append(poolName).append("\"");

      if (useCcm != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.USE_CCM).append("=\"").append(useCcm).append("\"");

      if (sharable != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.SHARABLE).append("=\"").append(sharable).append("\"");

      if (enlistment != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.ENLISTMENT).append("=\"").append(enlistment).append("\"");

      if (connectable != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.CONNECTABLE).append("=\"").
            append(connectable).append("\"");

      if (tracking != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.TRACKING).append("=\"").append(tracking).append("\"");

      if (mcp != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.MCP).append("=\"").append(mcp).append("\"");

      if (enlistmentTrace != null)
         sb.append(" ").append(ConnectionDefinition.Attribute.ENLISTMENT_TRACE).append("=\"")
            .append(enlistmentTrace).append("\"");

      sb.append(">");

      if (configProperties != null && configProperties.size() > 0)
      {
         Iterator<Map.Entry<String, String>> it = configProperties.entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            sb.append("<").append(ConnectionDefinition.Tag.CONFIG_PROPERTY);
            sb.append(" name=\"").append(entry.getKey()).append("\">");
            sb.append(entry.getValue());
            sb.append("</").append(ConnectionDefinition.Tag.CONFIG_PROPERTY).append(">");
         }
      }

      if (pool != null)
         sb.append(pool);

      if (security != null)
         sb.append(security);

      if (timeOut != null)
         sb.append(timeOut);

      if (validation != null)
         sb.append(validation);

      if (recovery != null)
         sb.append(recovery);

      sb.append("</connection-definition>");
      
      return sb.toString();
   }
}
