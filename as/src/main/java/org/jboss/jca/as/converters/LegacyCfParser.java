/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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
package org.jboss.jca.as.converters;

import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Logger;

/**
 * Parser for legacy ds.xml
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class LegacyCfParser extends AbstractParser
{
   private static final String DEFAULT_SECURITY_DOMAIN = "other";
   private static Logger log = Logger.getLogger(LegacyCfParser.class);
   
   /**
    * parse xml string to connection factory
    * @param xmlInputStream xml file input stream
    * @return ConnectionFactories
    * @throws Exception exception
    */
   public ConnectionFactories parse(InputStream xmlInputStream) throws Exception
   {
      XMLStreamReader reader = null;

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      try
      {
         return parse(reader);
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
   }

   private void skipParse(XMLStreamReader reader) throws Exception
   {
      int level = 1;
      while (reader.hasNext() && level > 0)
      {
         switch (reader.next()) 
         {
            case END_ELEMENT : 
            {
               level--;
               break;
            }
            case START_ELEMENT : 
            {
               level++;
               break;
            }
            default :
               continue;
         }
      }
      log.info("Skipping: " + reader.getLocalName());
   }
   

   private void notSupport(XMLStreamReader reader) throws Exception
   {
      log.info("So far not support " + reader.getLocalName());
   }

   private ConnectionFactories parse(XMLStreamReader reader) throws Exception
   {

      ConnectionFactories connectionFactories = null;

      //iterate over tags
      int iterate;
      try
      {
         iterate = reader.nextTag();
      }
      catch (XMLStreamException e)
      {
         //founding a non tag..go on. Normally non-tag found at beginning are comments or DTD declaration
         iterate = reader.nextTag();
      }
      switch (iterate)
      {
         case END_ELEMENT : {
            // should mean we're done, so ignore it.
            break;
         }
         case START_ELEMENT : {

            switch (Tag.forName(reader.getLocalName()))
            {
               case DATASOURCES : {
                  notSupport(reader);
                  return null;
               }
               case CONNECTION_FACTORIES : {
                  connectionFactories = parseConnectionFactories(reader);
                  break;
               }
               default :
                  throw new UnknownTagException(reader.getLocalName());
            }
            break;
         }
         default :
            throw new IllegalStateException();
      }

      return connectionFactories;

   }

   private ConnectionFactories parseConnectionFactories(XMLStreamReader reader) throws Exception
   {
      ArrayList<NoTxConnectionFactory> noTxConnectionFactory = new ArrayList<NoTxConnectionFactory>();
      ArrayList<TxConnectionFactory> txConnectionFactory = new ArrayList<TxConnectionFactory>();
      ArrayList<Mbean> mbean = new ArrayList<Mbean>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTION_FACTORIES)
               {
                  //merge mbean infomation
                  if ((noTxConnectionFactory.size() == 1 ||
                     txConnectionFactory.size() == 1) &&
                     noTxConnectionFactory.size() + txConnectionFactory.size() == 1 &&
                     mbean.size() == 1)
                  {
                     String jndiName = mbean.get(0).getAttributes().get("JNDIName");
                     if (jndiName != null)
                     {
                        Map<String, String> cps = new HashMap<String, String>();
                        LegacyConnectionFactoryImp cfImpl = null;
                        if (noTxConnectionFactory.size() == 1)
                           cfImpl = (LegacyConnectionFactoryImp)noTxConnectionFactory.get(0);
                        else
                           cfImpl = (LegacyConnectionFactoryImp)txConnectionFactory.get(0);
                        if (!jndiName.startsWith("java"))
                           jndiName = "java:/" + jndiName;
                        String props = mbean.get(0).getAttributes().get("Properties");
                        String[] strs = null;
                        if (props != null)
                           strs = props.split(" ");
                        if (strs != null && strs.length > 0)
                        {
                           for (String str : strs)
                           {
                              String[] prop = str.split("=");
                              cps.put(prop[0], prop[1]);
                           }
                        }
                        cfImpl.buildAdminObejcts("FIXME", jndiName, "", cps, 
                           Defaults.ENABLED, Defaults.USE_JAVA_CONTEXT);
                        cfImpl.buildResourceAdapterImpl();
                     }
                  }
                  return new ConnectionFactoriesImpl(noTxConnectionFactory, txConnectionFactory);
               }
               else
               {
                  if (ConnectionFactories.Tag.forName(reader.getLocalName()) == ConnectionFactories.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ConnectionFactories.Tag.forName(reader.getLocalName()))
               {
                  case NO_TX_CONNECTION_FACTORY : {
                     noTxConnectionFactory.add(parseNoTxConnectionFactory(reader));
                     break;
                  }
                  case TX_CONNECTION_FACTORY : {
                     txConnectionFactory.add(parseTxConnectionFactory(reader));
                     break;
                  }
                  case MBEAN : {
                     mbean.add(parseMbean(reader));
                     break;
                  }
                  default :
                     skipParse(reader);
               }
               break;
            }
         }
      }
      throw new ParserException(reader.getLocalName());
   }

   private NoTxConnectionFactory parseNoTxConnectionFactory(XMLStreamReader reader) throws Exception
   {
      Map<String, String> configProperty = new HashMap<String, String>();
      String rarName = null;
      String connectionDefinition = null;

      String poolName = null;

      String jndiName = null;

      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      Boolean prefill = Defaults.PREFILL;
      
      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;

      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      //Integer xaResourceTimeout = null;

      Boolean backgroundValidation = Defaults.BACKGROUND_VALIDATION;
      Long backgroundValidationMillis = null;
      Boolean useFastFail = Defaults.USE_FAST_FAIL;
      String securityDomainManaged = "";
      String securityDomainAndApplicationManaged = "";
      boolean applicationManaged = true;
      
      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ConnectionFactories.Tag.forName(reader.getLocalName()) == 
                  ConnectionFactories.Tag.NO_TX_CONNECTION_FACTORY)
               {
                  LegacyConnectionFactoryImp cfImpl = new LegacyConnectionFactoryImp(jndiName, rarName, 
                        configProperty, poolName, connectionDefinition, configProperty, 
                        TransactionSupportEnum.NoTransaction);
                  cfImpl.buildTimeOut(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                        allocationRetryWaitMillis, null);
                  cfImpl.buildSecurity(securityDomainManaged, securityDomainAndApplicationManaged, applicationManaged);
                  cfImpl.buildValidation(backgroundValidation, backgroundValidationMillis, useFastFail);
                  cfImpl.buildCommonPool(minPoolSize, maxPoolSize, prefill, null, Defaults.NO_TX_SEPARATE_POOL, 
                        Defaults.INTERLEAVING);
                  cfImpl.buildResourceAdapterImpl();
                  return cfImpl;
               }
               else
               {
                  if (NoTxConnectionFactory.Tag.forName(reader.getLocalName()) == NoTxConnectionFactory.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (NoTxConnectionFactory.Tag.forName(reader.getLocalName()))
               {
                  case SECURITY_DOMAIN : {
                     securityDomainManaged = elementAsString(reader);
                     applicationManaged = false;
                     break;
                  }
                  case SECURITY_DOMAIN_AND_APPLICATION : {
                     securityDomainAndApplicationManaged = elementAsString(reader);
                     applicationManaged = false;
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case RAR_NAME : {
                     rarName = elementAsString(reader);
                     break;
                  }
                  case CONNECTION_DEFINITION : {
                     elementAsString(reader);
                     connectionDefinition = "FIXME_MCF_CLASS_NAME";
                     break;
                  }
                  case JNDI_NAME : {
                     poolName = elementAsString(reader);
                     if (poolName.startsWith("java:"))
                        poolName = poolName.substring(5);

                     jndiName = "java:jboss/" + poolName;
                     break;
                  }
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader);
                     break;
                  }
                  case USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     skipParse(reader);

               }
               break;
            }
            case CHARACTERS : {
               break;
            }
         }
      }
      throw new ParserException();
   }

   private TxConnectionFactory parseTxConnectionFactory(XMLStreamReader reader) throws Exception
   {
      Map<String, String> configProperty = new HashMap<String, String>();
      String rarName = null;
      String connectionDefinition = null;

      String poolName = null;

      String jndiName = null;

      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      Boolean prefill = Defaults.PREFILL;
      
      Long blockingTimeoutMillis = null;
      Long idleTimeoutMinutes = null;

      Integer allocationRetry = null;
      Long allocationRetryWaitMillis = null;
      Integer xaResourceTimeout = null;

      Boolean backgroundValidation = Defaults.BACKGROUND_VALIDATION;
      Long backgroundValidationMillis = null;
      Boolean useFastFail = Defaults.USE_FAST_FAIL;
      
      TransactionSupportEnum transactionSupport = TransactionSupportEnum.LocalTransaction;
      Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;
      Boolean interleaving = Defaults.INTERLEAVING;

      String securityDomainManaged = "";
      String securityDomainAndApplicationManaged = "";
      boolean applicationManaged = true;
      
      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ConnectionFactories.Tag.forName(reader.getLocalName()) == 
                  ConnectionFactories.Tag.TX_CONNECTION_FACTORY)
               {
                  LegacyConnectionFactoryImp cfImpl = new LegacyConnectionFactoryImp(jndiName, rarName, 
                        configProperty, poolName, connectionDefinition, configProperty, transactionSupport);
                  cfImpl.buildTimeOut(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                        allocationRetryWaitMillis, xaResourceTimeout);
                  cfImpl.buildSecurity(securityDomainManaged, securityDomainAndApplicationManaged, applicationManaged);
                  cfImpl.buildValidation(backgroundValidation, backgroundValidationMillis, useFastFail);
                  cfImpl.buildCommonPool(minPoolSize, maxPoolSize, prefill, null, noTxSeparatePool, interleaving);
                  cfImpl.buildResourceAdapterImpl();
                  return cfImpl;
               }
               else
               {
                  if (TxConnectionFactory.Tag.forName(reader.getLocalName()) == TxConnectionFactory.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (TxConnectionFactory.Tag.forName(reader.getLocalName()))
               {
                  case SECURITY_DOMAIN : {
                     securityDomainManaged = elementAsString(reader);
                     applicationManaged = false;
                     break;
                  }
                  case SECURITY_DOMAIN_AND_APPLICATION : {
                     securityDomainAndApplicationManaged = elementAsString(reader);
                     applicationManaged = false;
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case RAR_NAME : {
                     rarName = elementAsString(reader);
                     break;
                  }
                  case CONNECTION_DEFINITION : {
                     elementAsString(reader);
                     connectionDefinition = "FIXME_MCF_CLASS_NAME";
                     break;
                  }
                  case JNDI_NAME : {
                     poolName = elementAsString(reader);
                     if (poolName.startsWith("java:"))
                        poolName = poolName.substring(5);

                     jndiName = "java:jboss/" + poolName;
                     break;
                  }
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader);
                     break;
                  }
                  case USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  case LOCAL_TRANSACTION : {
                     transactionSupport = TransactionSupportEnum.LocalTransaction;
                     break;
                  }
                  case XA_TRANSACTION : {
                     transactionSupport = TransactionSupportEnum.XATransaction;
                     break;
                  }
                  case NO_TX_SEPARATE_POOLS : {
                     noTxSeparatePool = elementAsBoolean(reader);
                     break;
                  }
                  case XA_RESOURCE_TIMEOUT : {
                     xaResourceTimeout = elementAsInteger(reader);
                     break;
                  }
                  case TRACK_CONNECTION_BY_TX : {
                     interleaving = false;
                     break;
                  }
                  default :
                     skipParse(reader);

               }
               break;
            }
            case CHARACTERS : {
               break;
            }
         }
      }
      throw new ParserException();
   }

   private Mbean parseMbean(XMLStreamReader reader) throws Exception
   {
      Map<String, String> attributes = new HashMap<String, String>();
      String code = null;
      String name = null;

      //attributes reading
      for (Mbean.Attribute attribute : Mbean.Attribute.values())
      {
         switch (attribute)
         {
            case CODE : {
               code = attributeAsString(reader, "code");
               break;
            }
            case NAME : {
               name = attributeAsString(reader, "name");
               break;
            }
            default :
               break;
         }
      }
      //elements reading
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ConnectionFactories.Tag.forName(reader.getLocalName()) == 
                  ConnectionFactories.Tag.MBEAN)
               {
                  MbeanImpl mbeanImpl = new MbeanImpl(code, name, attributes);
                  return mbeanImpl;
               }
               else
               {
                  if (Mbean.Tag.forName(reader.getLocalName()) == Mbean.Tag.UNKNOWN)
                  {
                     throw new UnknownTagException(reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Mbean.Tag.forName(reader.getLocalName()))
               {
                  case ATTRIBUTE : {
                     attributes.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case DEPENDS : {
                     attributeAsString(reader, "optional-attribute-name");
                     elementAsString(reader);
                     break;
                  }
                  default :
                     skipParse(reader);

               }
               break;
            }
            case CHARACTERS : {
               break;
            }
         }
      }
      throw new ParserException();
   }
   
   /**
   *
   * A Tag.
   */
   public enum Tag 
   {
      /**
       * always first
       */
      UNKNOWN(null),

      /**
       * datasources tag
       */
      DATASOURCES("datasources"),

      /**
       * connection-factories tag
       */
      CONNECTION_FACTORIES("connection-factories");

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
      * Static method to get enum instance given localName string
      *
      * @param localName a string used as localname (typically tag name as defined in xsd)
      * @return the enum instance
      */
      public static Tag forName(String localName)
      {
         final Tag element = MAP.get(localName);
         return element == null ? UNKNOWN : element;
      }

   }
}
