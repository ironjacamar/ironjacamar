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
package org.jboss.jca.common.metadata.resourceadapter;

import org.jboss.jca.common.api.metadata.common.SecurityManager;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject;
import org.jboss.jca.common.api.metadata.resourceadapter.AdminObject.Attribute;
import org.jboss.jca.common.api.metadata.resourceadapter.LocalTxConnectionFactory;
import org.jboss.jca.common.api.metadata.resourceadapter.NoTxConnectionFactory;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.api.metadata.resourceadapter.Security;
import org.jboss.jca.common.api.metadata.resourceadapter.TimeOut;
import org.jboss.jca.common.api.metadata.resourceadapter.Validation;
import org.jboss.jca.common.api.metadata.resourceadapter.XaTxConnectionFactory;
import org.jboss.jca.common.metadata.AbstractParser;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 * A ResourceAdapterParserr.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class ResourceAdapterParser extends AbstractParser implements MetadataParser<ResourceAdapter>
{

   @Override
   public ResourceAdapter parse(InputStream xmlInputStream) throws Exception
   {

      XMLStreamReader reader = null;
      ResourceAdapter adapter = null;

      try
      {
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         reader = inputFactory.createXMLStreamReader(xmlInputStream);

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
                  case RESOURCE_ADPTER : {
                     adapter = parseResourceAdapter(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }

               break;
            }
            default :
               throw new IllegalStateException();
         }
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
      return adapter;

   }

   private ResourceAdapter parseResourceAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<NoTxConnectionFactory> connectionFactories = new ArrayList<NoTxConnectionFactory>();
      ArrayList<AdminObject> adminObjects = new ArrayList<AdminObject>();
      String archive = null;
      TransactionSupportEnum transactionSupport = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.RESOURCE_ADPTER)
               {
                  return new ResourceAdapterImpl(archive, transactionSupport, connectionFactories, adminObjects);
               }
               else
               {
                  if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ResourceAdapter.Tag.forName(reader.getLocalName()))
               {
                  case ADMINOBJECT : {
                     adminObjects.add(parseAdminObjects(reader));
                     break;
                  }
                  case LOCALTXCONNECTIONFACTORY : {
                     connectionFactories.add(parseLocalTxConnectionFactrory(reader));
                     break;
                  }
                  case NOTXCONNECTIONFACTORY : {
                     connectionFactories.add(parseNoTxConnectionFactrory(reader));
                     break;
                  }
                  case XATXCONNECTIONFACTORY : {
                     connectionFactories.add(parseXaTxConnectionFactrory(reader));
                     break;
                  }
                  case TRANSACTIONSUPPORT : {
                     transactionSupport = TransactionSupportEnum.valueOf(elementAsString(reader));
                     break;
                  }
                  case ARCHIVE : {
                     archive = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private XaTxConnectionFactory parseXaTxConnectionFactrory(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      HashMap<String, String> configProperties = new HashMap<String, String>();
      String connectionDefinition = null;
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      String userName = null;
      Security security = null;
      TimeOut timeOut = null;
      boolean prefill = false;
      Validation validation = null;
      String password = null;
      boolean trackConnectionByTx = false;
      boolean noTxSeparatePools = false;
      Long xaResourceTimeout = null;

      //attributes reading
      boolean useJavaContext = false;
      String className = null;
      boolean enabled = true;
      String jndiName = null;
      String poolName = null;

      for (XaTxConnectionFactory.Attribute attribute : XaTxConnectionFactory.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case CLASS_NAME : {
               className = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.XATXCONNECTIONFACTORY)
               {

                  return new XaTxConnectionFactoryImpl(minPoolSize, maxPoolSize, prefill, userName, password,
                        connectionDefinition, configProperties, security, timeOut, validation, poolName, className,
                        jndiName, enabled, useJavaContext, noTxSeparatePools, trackConnectionByTx, xaResourceTimeout);
               }
               else
               {
                  if (XaTxConnectionFactory.Tag.forName(reader.getLocalName()) == XaTxConnectionFactory.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XaTxConnectionFactory.Tag.forName(reader.getLocalName()))
               {
                  case CONFIGPROPERTY : {
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case CONNECTIONDEFINITION : {
                     connectionDefinition = elementAsString(reader);
                     break;
                  }
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MINPOOLSIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case USERNAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case SECURITY : {
                     security = parseSecurity(reader);
                     break;
                  }
                  case TIMEOUT : {
                     timeOut = parseTimeOut(reader);
                     break;
                  }
                  case VALIDATION : {
                     validation = parseValidation(reader);
                     break;
                  }
                  case NOTXSEPARATEPOOLS : {
                     noTxSeparatePools = elementAsBoolean(reader);
                     break;
                  }
                  case TRACKCONNECTIONBYTX : {
                     trackConnectionByTx = elementAsBoolean(reader);
                     break;
                  }
                  case XARESOURCETIMEOUT : {
                     xaResourceTimeout = elementAsLong(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private LocalTxConnectionFactory parseLocalTxConnectionFactrory(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      HashMap<String, String> configProperties = new HashMap<String, String>();
      String connectionDefinition = null;
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      String userName = null;
      Security security = null;
      TimeOut timeOut = null;
      boolean prefill = false;
      Validation validation = null;
      String password = null;
      boolean trackConnectionByTx = false;
      boolean noTxSeparatePools = false;

      //attributes reading
      boolean useJavaContext = false;
      String className = null;
      boolean enabled = true;
      String jndiName = null;
      String poolName = null;

      for (LocalTxConnectionFactory.Attribute attribute : LocalTxConnectionFactory.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case CLASS_NAME : {
               className = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.LOCALTXCONNECTIONFACTORY)
               {

                  return new LocalTxConnectionFactoryImpl(minPoolSize, maxPoolSize, prefill, userName, password,
                        connectionDefinition, configProperties, security, timeOut, validation, poolName, className,
                        jndiName, enabled, useJavaContext, noTxSeparatePools, trackConnectionByTx);
               }
               else
               {
                  if (LocalTxConnectionFactory.Tag.
                        forName(reader.getLocalName()) == LocalTxConnectionFactory.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (LocalTxConnectionFactory.Tag.forName(reader.getLocalName()))
               {
                  case CONFIGPROPERTY : {
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case CONNECTIONDEFINITION : {
                     connectionDefinition = elementAsString(reader);
                     break;
                  }
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MINPOOLSIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case USERNAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case SECURITY : {
                     security = parseSecurity(reader);
                     break;
                  }
                  case TIMEOUT : {
                     timeOut = parseTimeOut(reader);
                     break;
                  }
                  case VALIDATION : {
                     validation = parseValidation(reader);
                     break;
                  }
                  case NOTXSEPARATEPOOLS : {
                     noTxSeparatePools = elementAsBoolean(reader);
                     break;
                  }
                  case TRACKCONNECTIONBYTX : {
                     trackConnectionByTx = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private NoTxConnectionFactory parseNoTxConnectionFactrory(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      HashMap<String, String> configProperties = new HashMap<String, String>();
      String connectionDefinition = null;
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      String userName = null;
      Security security = null;
      TimeOut timeOut = null;
      boolean prefill = false;
      Validation validation = null;
      String password = null;

      //attributes reading
      boolean useJavaContext = false;
      String className = null;
      boolean enabled = true;
      String jndiName = null;
      String poolName = null;

      for (NoTxConnectionFactory.Attribute attribute : NoTxConnectionFactory.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case CLASS_NAME : {
               className = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.NOTXCONNECTIONFACTORY)
               {

                  return new NoTxConnectionFactoryImpl(minPoolSize, maxPoolSize, prefill, userName, password,
                        connectionDefinition, configProperties, security, timeOut, validation, poolName, className,
                        jndiName, enabled, useJavaContext);
               }
               else
               {
                  if (NoTxConnectionFactory.Tag.forName(reader.getLocalName()) == NoTxConnectionFactory.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (NoTxConnectionFactory.Tag.forName(reader.getLocalName()))
               {
                  case CONFIGPROPERTY : {
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  case CONNECTIONDEFINITION : {
                     connectionDefinition = elementAsString(reader);
                     break;
                  }
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MINPOOLSIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case USERNAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case SECURITY : {
                     security = parseSecurity(reader);
                     break;
                  }
                  case TIMEOUT : {
                     timeOut = parseTimeOut(reader);
                     break;
                  }
                  case VALIDATION : {
                     validation = parseValidation(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Validation parseValidation(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Integer allocationRetry = null;
      boolean useFastFail = false;
      boolean backgroundValidation = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (NoTxConnectionFactory.Tag.forName(reader.getLocalName()) == NoTxConnectionFactory.Tag.VALIDATION)
               {

                  return new ValidationImpl(allocationRetry, backgroundValidation, useFastFail);
               }
               else
               {
                  if (Validation.Tag.forName(reader.getLocalName()) == Validation.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Validation.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATIONRETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case BACKGROUNDVALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case USEFASTFAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private TimeOut parseTimeOut(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Long blockingTimeoutMillis = null;
      Long backgroundValidationMinutes = null;
      Long allocationRetryWaitMillis = null;
      Long idleTimeoutMinutes = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (NoTxConnectionFactory.Tag.forName(reader.getLocalName()) == NoTxConnectionFactory.Tag.TIMEOUT)
               {

                  return new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetryWaitMillis,
                        backgroundValidationMinutes);
               }
               else
               {
                  if (TimeOut.Tag.forName(reader.getLocalName()) == TimeOut.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (TimeOut.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATIONRETRYWAITMILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case BACKGROUNDVALIDATIONMINUTES : {
                     backgroundValidationMinutes = elementAsLong(reader);
                     break;
                  }
                  case BLOCKINGTIMEOUTMILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case IDLETIMEOUTMINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private Security parseSecurity(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      SecurityManager securityManager = null;
      String securityDomain = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (NoTxConnectionFactory.Tag.forName(reader.getLocalName()) == NoTxConnectionFactory.Tag.SECURITY)
               {

                  return new SecurityImpl(securityManager, securityDomain);
               }
               else
               {
                  if (Security.Tag.forName(reader.getLocalName()) == Security.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Security.Tag.forName(reader.getLocalName()))
               {
                  case SECURITYDOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case SECURITYMANAGER : {
                     securityManager = SecurityManager.valueOf(elementAsString(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private AdminObject parseAdminObjects(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      HashMap<String, String> configProperties = new HashMap<String, String>();

      //attributes reading
      boolean useJavaContext = false;
      String className = null;
      boolean enabled = true;
      String jndiName = null;

      for (Attribute attribute : AdminObject.Attribute.values())
      {
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case CLASS_NAME : {
               className = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName());
               break;
            }
            default :
               break;
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.ADMINOBJECT)
               {

                  return new AdminObjectImpl(configProperties, className, jndiName, enabled, useJavaContext);
               }
               else
               {
                  if (AdminObject.Tag.forName(reader.getLocalName()) == AdminObject.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (AdminObject.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

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

      /** jboss-ra tag name
       *
       */
      RESOURCE_ADPTER("resource-adapter");

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
