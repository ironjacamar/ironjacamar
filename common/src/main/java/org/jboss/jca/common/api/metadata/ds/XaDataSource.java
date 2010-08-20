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
package org.jboss.jca.common.api.metadata.ds;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * A XaDataSource.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public interface XaDataSource extends CommonDataSource
{

   /**
    * Get the userName.
    *
    * @return the userName.
    */
   public String getUserName();

   /**
    * Get the password.
    *
    * @return the password.
    */
   public String getPassword();


   /**
    * Get the xaDataSourceClass.
    *
    * @return the xaDataSourceClass.
    */
   public String getXaDataSourceClass();


   /**
    * Get the isSameRmOverride.
    *
    * @return the isSameRmOverride.
    */
   public boolean isSameRmOverride();

   /**
    * Get the interleaving.
    *
    * @return the interleaving.
    */
   public boolean isInterleaving();

   /**
    * Get the recoverySettings.
    *
    * @return the recoverySettings.
    */
   public Recovery getRecovery();


   /**
    * Get the statement.
    *
    * @return the statement.
    */
   public Statement getStatement();


   /**
    * Get the urlDelimiter.
    *
    * @return the urlDelimiter.
    */
   public String getUrlDelimiter();

   /**
    * Get the urlSelectorStrategyClassName.
    *
    * @return the urlSelectorStrategyClassName.
    */
   public String getUrlSelectorStrategyClassName();

   /**
    * Get the newConnectionSql.
    *
    * @return the newConnectionSql.
    */
   public String getNewConnectionSql();

   /**
    * Get the xaDataSourceProperty.
    *
    * @return the xaDataSourceProperty.
    */
   public Map<String, String> getXaDataSourceProperty();

   /**
    * Get the padXid.
    *
    * @return the padXid.
    */
   public boolean isPadXid();

   /**
    * Get the wrapXaDataSource.
    *
    * @return the wrapXaDataSource.
    */
   public boolean isWrapXaDataSource();

   /**
    * Get the noTxSeparatePool.
    *
    * @return the noTxSeparatePool.
    */
   public boolean isNoTxSeparatePool();

   /**
    * Get the trackConnectionByTx.
    *
    * @return the trackConnectionByTx.
    */
   public boolean isTrackConnectionByTx();

   /**
   *
   * A Tag.
   *
   * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
   *
   */
   public enum Tag
   {
      /** always first
       *
       */
      UNKNOWN(null),

      /**
      * minPoolSize tag
      */
      MINPOOLSIZE("min-pool-size"),
      /**
      * maxPoolSize tag
      */
      MAXPOOLSIZE("max-pool-size"),
      /**
      * prefill tag
      */
      PREFILL("prefill"),
      /**
      * userName tag
      */
      USERNAME("user-name"),
      /**
      * password tag
      */
      PASSWORD("password"),
      /**
      * xaDatasourceProperty tag
      */
      XADATASOURCEPROPERTY("xa-datasource-property"),
      /**
      * xaDatasourceClass tag
      */
      XADATASOURCECLASS("xa-datasource-class"),
      /**
      * transactionIsolation tag
      */
      TRANSACTIONISOLATION("transaction-isolation"),
      /**
      * isSameRmOverrideValue tag
      */
      ISSAMERMOVERRIDEVALUE("is-same-rm-override"),
      /**
      * interleaving tag
      */
      INTERLEAVING("interleaving"),
      /**
      * recoverySettings tag
      */
      RECOVERY("recovery"),
      /**
      * timeOut tag
      */
      TIMEOUT("time-out"),
      /**
      * security tag
      */
      SECURITY("security"),
      /**
      * statement tag
      */
      STATEMENT("statement"),
      /**
      * validation tag
      */
      VALIDATION("validation"),
      /**
      * urlDelimiter tag
      */
      URLDELIMITER("url-delimiter"),
      /**
      * urlSelectorStrategyClassName tag
      */
      URLSELECTORSTRATEGYCLASSNAME("url-selector-strategy-class-name"),
      /**
      * newConnectionSql tag
      */
      NEWCONNECTIONSQL("new-connection-sql"),

      /**
       * pad-xid tag
       */
      PAD_XID("pad-xid"),

      /**
       * wrap-xa-resource tag
       */
      WRAP_XA_RESOURCE("wrap-xa-resource"),

      /**
       * no-tx-separate-pools tag
       */
      NO_TX_SEPARATE_POOLS("no-tx-separate-pools"),

      /**
       * track-connection-by-tx tag
       */
      TRACK_CONNECTION_BY_TX("track-connection-by-tx");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Tag(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

      private static final Map<String, Tag> MAP;

      static
      {
         final Map<String, Tag> map = new HashMap<String, Tag>();
         for (Tag element : values())
         {
            final String name = element.getLocalName();
            if (name != null)
               map.put(name, element);
         }
         MAP = map;
      }

      /**
      *
      * Static method to get enum instance given localName XsdString
      *
      * @param localName a XsdString used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }

   /**
    *
    * A Attribute.
    *
    * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
    *
    */
   public enum Attribute
   {

      /** jndiName attribute
       *
       */
      JNDINAME("jndi-name"),

      /** jndiName attribute
      *
      */
      POOL_NAME("pool-name"),

      /** jndiName attribute
      *
      */
      ENABLED("enabled"),
      /** jndiName attribute
      *
      */
      USEJAVACONTEXT("use-java-context");

      private final String name;

      /**
       *
       * Create a new Tag.
       *
       * @param name a name
       */
      Attribute(final String name)
      {
         this.name = name;
      }

      /**
       * Get the local name of this element.
       *
       * @return the local name
       */
      public String getLocalName()
      {
         return name;
      }

   }
}
