/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2021, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.CommonLogger;
import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.Capacity;
import org.jboss.jca.common.api.metadata.common.Credential;
import org.jboss.jca.common.api.metadata.common.Extension;
import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.common.Pool;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.Security;
import org.jboss.jca.common.api.metadata.common.TimeOut;
import org.jboss.jca.common.api.metadata.common.Validation;
import org.jboss.jca.common.api.metadata.common.XaPool;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ParserException;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

/**
 *
 * A AbstractParser.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public abstract class AbstractParser
{
   /** The logger */
   protected static CommonLogger log = Logger.getMessageLogger(CommonLogger.class, AbstractParser.class.getName());

   /** The bundle */
   protected static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /** Resolve system property */
   private boolean resolveSystemProperties = true;

   /**
    * {@inheritDoc}
    */
   public boolean isSystemPropertiesResolved()
   {
      return resolveSystemProperties;
   }

   /**
    * {@inheritDoc}
    */
   public void setSystemPropertiesResolved(boolean v)
   {
      resolveSystemProperties = v;
   }

   /**
    * convert an xml element in boolean value. Empty elements results with true (tag presence is sufficient condition)
    *
    * @param reader the StAX reader
    * @return the boolean representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case of non valid boolean for given element value
    */
   protected Boolean elementAsBoolean(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String elementtext = rawElementText(reader);
      String stringValue = getSubstitutionValue(elementtext);
      if (stringValue == null || stringValue.length() == 0 || stringValue.trim().equalsIgnoreCase("true") ||
          stringValue.trim().equalsIgnoreCase("false"))
      {
         return stringValue == null || stringValue.length() == 0 ? Boolean.TRUE : Boolean.valueOf(stringValue.trim());
      }
      else
      {
         throw new ParserException(bundle.elementAsBoolean(elementtext, reader.getLocalName()));
      }
   }

   /**
    * convert an xml attribute in boolean value. Empty elements results with false
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @param defaultValue  defaultValue
    * @return the boolean representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case of not valid boolena for given attribute
    */
   protected Boolean attributeAsBoolean(XMLStreamReader reader, String attributeName, Boolean defaultValue)
      throws XMLStreamException, ParserException
   {
      String attributeString = rawAttributeText(reader, attributeName);
      String stringValue = getSubstitutionValue(attributeString);
      if (stringValue == null || stringValue.length() == 0 || stringValue.trim().equalsIgnoreCase("true") ||
          stringValue.trim().equalsIgnoreCase("false"))
      {

         return attributeString == null
            ? defaultValue :
            Boolean.valueOf(reader.getAttributeValue("", attributeName).trim());
      }
      else
      {
         throw new ParserException(bundle.attributeAsBoolean(attributeString, reader.getLocalName()));
      }
   }

   /**
    * convert an xml element in String value
    *
    * @param reader the StAX reader
    * @return the string representing element
    * @throws XMLStreamException StAX exception
    */
   protected String elementAsString(XMLStreamReader reader) throws XMLStreamException
   {
      String elementtext = rawElementText(reader);
      return getSubstitutionValue(elementtext);
   }

   /**
    * FIXME Comment this
    *
    * @param reader
    * @return the string representing the raw eleemnt text
    * @throws XMLStreamException
    */
   private String rawElementText(XMLStreamReader reader) throws XMLStreamException
   {
      String elementtext = reader.getElementText();
      elementtext = elementtext == null ? null : elementtext.trim();
      return elementtext;
   }

   /**
    * convert an xml element in String value
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @return the string representing element
    * @throws XMLStreamException StAX exception
    */
   protected String attributeAsString(XMLStreamReader reader, String attributeName) throws XMLStreamException
   {
      String attributeString = rawAttributeText(reader, attributeName);
      return getSubstitutionValue(attributeString);
   }

   /**
    * convert an xml element in String value
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @return the string representing element
    * @throws XMLStreamException StAX exception
    */
   protected Integer attributeAsInt(XMLStreamReader reader, String attributeName) throws XMLStreamException
   {
      String attributeString = getSubstitutionValue(rawAttributeText(reader, attributeName));
      return attributeString != null ? Integer.valueOf(getSubstitutionValue(attributeString)) : null;
   }

   /**
    * FIXME Comment this
    *
    * @param reader
    * @param attributeName
    * @return the string representing raw attribute textx
    */
   private String rawAttributeText(XMLStreamReader reader, String attributeName)
   {
      String attributeString = reader.getAttributeValue("", attributeName) == null ? null : reader.getAttributeValue(
         "", attributeName)
            .trim();
      return attributeString;
   }

   /**
    * convert an xml element in Integer value
    *
    * @param reader the StAX reader
    * @return the integer representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case it isn't a number
    */
   protected Integer elementAsInteger(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Integer integerValue;
      integerValue = null;
      String elementtext = rawElementText(reader);
      try
      {
         integerValue = Integer.valueOf(getSubstitutionValue(elementtext));
      }
      catch (NumberFormatException nfe)
      {
         throw new ParserException(bundle.notValidNumber(elementtext, reader.getLocalName()));
      }
      return integerValue;
   }

   /**
    * convert an xml element in Long value
    *
    * @param reader the StAX reader
    * @return the long representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case it isn't a number
    */
   protected Long elementAsLong(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Long longValue;
      longValue = null;
      String elementtext = rawElementText(reader);

      try
      {
         longValue = Long.valueOf(getSubstitutionValue(elementtext));
      }
      catch (NumberFormatException nfe)
      {
         throw new ParserException(bundle.notValidNumber(elementtext, reader.getLocalName()));
      }

      return longValue;
   }

   /**
    * convert an xml element in FlushStrategy value
    *
    * @param reader the StAX reader
    * @return the flush strategy represention
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case it isn't a number
    */
   protected FlushStrategy elementAsFlushStrategy(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String elementtext = rawElementText(reader);
      FlushStrategy result = FlushStrategy.forName(getSubstitutionValue(elementtext));

      if (result != FlushStrategy.UNKNOWN)
         return result;

      throw new ParserException(bundle.notValidFlushStrategy(elementtext));
   }

   /**
    *
    * parse a {@link Pool} object
    *
    * @param reader reader
    * @return the parsed {@link Pool} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected Pool parsePool(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      Boolean fair = Defaults.FAIR;
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.POOL)
               {
                  return new PoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
                                      flushStrategy, capacity, fair);
               }
               else
               {
                  if (Pool.Tag.forName(reader.getLocalName()) == Pool.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Pool.Tag.forName(reader.getLocalName()))
               {
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case FAIR : {
                     fair = elementAsBoolean(reader);
                     break;
                  }
                  case USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case CAPACITY : {
                     capacity = parseCapacity(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
   *
   * parse a {@link XaPool} object
   *
   * @param reader reader
   * @return the parsed {@link XaPool} object
   * @throws XMLStreamException XMLStreamException
   * @throws ParserException ParserException
    * @throws ValidateException ValidateException
   */
   protected XaPool parseXaPool(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      Boolean fair = Defaults.FAIR;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;
      Boolean interleaving = Defaults.INTERLEAVING;
      Boolean isSameRmOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean padXid = Defaults.PAD_XID;
      Boolean noTxSeparatePool = Defaults.NO_TX_SEPARATE_POOL;
      Boolean wrapXaDataSource = Defaults.WRAP_XA_RESOURCE;
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.XA_POOL)
               {

                  return new XaPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
                                        flushStrategy, capacity, fair,
                                        isSameRmOverride, interleaving, padXid,
                                        wrapXaDataSource, noTxSeparatePool);

               }
               else
               {
                  if (XaPool.Tag.forName(reader.getLocalName()) == XaPool.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XaPool.Tag.forName(reader.getLocalName()))
               {
                  case MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INTERLEAVING : {
                     interleaving = elementAsBoolean(reader);
                     break;
                  }
                  case IS_SAME_RM_OVERRIDE : {
                     isSameRmOverride = elementAsBoolean(reader);
                     break;
                  }
                  case NO_TX_SEPARATE_POOLS : {
                     noTxSeparatePool = elementAsBoolean(reader);
                     break;
                  }
                  case PAD_XID : {
                     padXid = elementAsBoolean(reader);
                     break;
                  }
                  case WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case FAIR : {
                     fair = elementAsBoolean(reader);
                     break;
                  }
                  case USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case CAPACITY : {
                     capacity = parseCapacity(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    *
    * parse a {@link Security} element
    *
    * @param reader reader
    * @return a {@link Security} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected Security parseSecuritySettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      String securityDomain = null;
      String securityDomainAndApplication = null;
      boolean application = Defaults.APPLICATION_MANAGED_SECURITY;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.SECURITY)
               {

                  return new SecurityImpl(securityDomain, securityDomainAndApplication,
                                          application);
               }
               else
               {
                  if (Security.Tag.forName(reader.getLocalName()) == Security.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Security.Tag.forName(reader.getLocalName()))
               {

                  case SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case SECURITY_DOMAIN_AND_APPLICATION : {
                     securityDomainAndApplication = elementAsString(reader);
                     break;
                  }
                  case APPLICATION : {
                     application = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   protected Security parseElytronSecuritySettings(XMLStreamReader reader) throws XMLStreamException, ParserException,
           ValidateException
   {

      String securityDomain = null;
      String securityDomainAndApplication = null;
      boolean application = Defaults.APPLICATION_MANAGED_SECURITY;
      boolean elytronEnabled = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.SECURITY)
               {

                  return new SecurityImpl(securityDomain, securityDomainAndApplication,
                          application, elytronEnabled);
               }
               else
               {
                  if (Security.Tag.forName(reader.getLocalName()) == Security.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Security.Tag.forName(reader.getLocalName()))
               {

                  case SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case SECURITY_DOMAIN_AND_APPLICATION : {
                     securityDomainAndApplication = elementAsString(reader);
                     break;
                  }
                  case APPLICATION : {
                     application = elementAsBoolean(reader);
                     break;
                  }
                  case ELYTRON : {
                     elytronEnabled = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * System property substitution
    * @param input The input string
    * @return The output
    */
   private String getSubstitutionValue(String input) throws XMLStreamException
   {
      if (input == null || input.trim().equals(""))
         return input;

      if (!resolveSystemProperties)
         return input;

      while ((input.indexOf("${")) != -1)
      {
         int from = input.indexOf("${");
         int to = input.indexOf("}");
         int dv = input.indexOf(":", from + 2);

         if (dv != -1)
         {
            if (dv > to)
               dv = -1;
         }

         String systemProperty = "";
         String defaultValue = "";
         String s = input.substring(from + 2, to);
         if (dv == -1)
         {
            if ("/".equals(s))
            {
               systemProperty = File.separator;
            }
            else if (":".equals(s))
            {
               systemProperty = File.pathSeparator;
            }
            else
            {
               systemProperty = SecurityActions.getSystemProperty(s);
            }
         }
         else
         {
            s = input.substring(from + 2, dv);
            systemProperty = SecurityActions.getSystemProperty(s);
            defaultValue = input.substring(dv + 1, to);
         }
         String prefix = "";
         String postfix = "";

         if (from != 0)
         {
            prefix = input.substring(0, from);
         }

         if (to + 1 < input.length() - 1)
         {
            postfix = input.substring(to + 1);
         }

         if (systemProperty != null && !systemProperty.trim().equals(""))
         {
            input = prefix + systemProperty + postfix;
         }
         else if (defaultValue != null && !defaultValue.trim().equals(""))
         {
            input = prefix + defaultValue + postfix;
         }
         else
         {
            input = prefix + postfix;
            log.debugf("System property %s not set", s);
         }
      }
      return input;
   }

   /**
    *
    * parse credential tag
    *
    * @param reader reader
    * @return the parse Object
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    * @throws ValidateException in case of error
    */
   protected Credential parseCredential(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      String userName = null;
      String password = null;
      String securityDomain = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.SECURITY ||
                   Recovery.Tag.forName(reader.getLocalName()) == Recovery.Tag.RECOVER_CREDENTIAL)
               {

                  return new CredentialImpl(userName, password, securityDomain);
               }
               else
               {
                  if (Credential.Tag.forName(reader.getLocalName()) == Credential.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Credential.Tag.forName(reader.getLocalName()))
               {
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USER_NAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }


   /**
    *
    * Parse recovery tag
    *
    * @param reader reader
    * @return the parsed recovery object
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    * @throws ValidateException in case of error
    */
   protected Recovery parseRecovery(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {

      Boolean noRecovery = null;
      Credential security = null;
      Extension plugin = null;

      for (Recovery.Attribute attribute : Recovery.Attribute.values())
      {
         switch (attribute)
         {
            case NO_RECOVERY : {
               noRecovery = attributeAsBoolean(reader, attribute.getLocalName(), Boolean.FALSE);
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
               if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.RECOVERY)
               {
                  return new Recovery(security, plugin, noRecovery);
               }
               else
               {
                  if (Recovery.Tag.forName(reader.getLocalName()) == Recovery.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               Recovery.Tag tag = Recovery.Tag.forName(reader.getLocalName());
               switch (tag)
               {
                  case RECOVER_CREDENTIAL : {
                     security = parseCredential(reader);
                     break;
                  }
                  case RECOVER_PLUGIN : {
                     plugin = parseExtension(reader, tag.getLocalName());
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    *
    * parse the Extension tag
    *
    * @param reader reader
    * @param enclosingTag enclosingTag
    * @return the parsed extension object
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    * @throws ValidateException in case of error
    */
   protected Extension parseExtension(XMLStreamReader reader, String enclosingTag) throws XMLStreamException,
      ParserException,
      ValidateException
   {

      String className = null;
      Map<String, String> properties = null;

      for (Extension.Attribute attribute : Extension.Attribute.values())
      {
         switch (attribute)
         {
            case CLASS_NAME : {
               className = attributeAsString(reader, attribute.getLocalName());
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
               if (reader.getLocalName().equals(enclosingTag))
               {
                  if (className == null)
                  {
                     throw new ParserException(bundle.missingClassName(enclosingTag));
                  }

                  return new Extension(className, null, properties);
               }
               else
               {
                  if (Extension.Tag.forName(reader.getLocalName()) == Extension.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Extension.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     if (properties == null)
                        properties = new HashMap<String, String>();
                     parseConfigProperty(properties, reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse capacity tag
    *
    * @param reader reader
    * @return the parsed recovery object
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    * @throws ValidateException in case of error
    */
   protected Capacity parseCapacity(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      Extension incrementer = null;
      Extension decrementer = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (org.jboss.jca.common.api.metadata.ds.DsPool.Tag.forName(reader.getLocalName()) ==
                   org.jboss.jca.common.api.metadata.ds.DsPool.Tag.CAPACITY)
               {
                  return new Capacity(incrementer, decrementer);
               }
               else
               {
                  if (Capacity.Tag.forName(reader.getLocalName()) == Capacity.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               Capacity.Tag tag = Capacity.Tag.forName(reader.getLocalName());
               switch (tag)
               {
                  case INCREMENTER : {
                     incrementer = parseExtension(reader, tag.getLocalName());
                     break;
                  }
                  case DECREMENTER : {
                     decrementer = parseExtension(reader, tag.getLocalName());
                     break;
                  }
                  default :
                     // Nothing
               }
               break;
            }
            default :
               throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }


   /**
    * Parse validation
    * @param reader The reader
    * @return The result
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected Validation parseValidation(XMLStreamReader reader) throws XMLStreamException, 
      ParserException, 
      ValidateException
   {
      Boolean validateOnMatch = Defaults.VALIDATE_ON_MATCH;
      Boolean useFastFail = Defaults.USE_FAST_FAIL;
      Boolean backgroundValidation = Defaults.BACKGROUND_VALIDATION;
      Long backgroundValidationMillis = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ConnectionDefinition.Tag.forName(reader.getLocalName()) == ConnectionDefinition.Tag.VALIDATION)
               {
                  return new ValidationImpl(validateOnMatch, backgroundValidation, backgroundValidationMillis,
                                            useFastFail);
               }
               else
               {
                  if (Validation.Tag.forName(reader.getLocalName()) == Validation.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (Validation.Tag.forName(reader.getLocalName()))
               {
                  case VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader);
                     break;
                  }
                  case BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }

   /**
    * Parse timeout
    * @param reader The reader
    * @param isXa XA flag
    * @return The result
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected TimeOut parseTimeOut(XMLStreamReader reader, Boolean isXa) throws XMLStreamException,
      ParserException, ValidateException
   {
      Long blockingTimeoutMillis = null;
      Long allocationRetryWaitMillis = null;
      Long idleTimeoutMinutes = null;
      Integer allocationRetry = null;
      Integer xaResourceTimeout = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ConnectionDefinition.Tag.forName(reader.getLocalName()) == ConnectionDefinition.Tag.TIMEOUT)
               {

                  return new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                         allocationRetryWaitMillis, xaResourceTimeout);
               }
               else
               {
                  if (TimeOut.Tag.forName(reader.getLocalName()) == TimeOut.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (TimeOut.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader);
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
                  case XA_RESOURCE_TIMEOUT : {
                     if (isXa != null && Boolean.FALSE.equals(isXa))
                        throw new ParserException(bundle.unsupportedElement(reader.getLocalName()));
                     xaResourceTimeout = elementAsInteger(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
               }
               break;
            }
         }
      }
      throw new ParserException(bundle.unexpectedEndOfDocument());
   }


   /**
    * 
    * Adds config property to the map
    * 
    * @param configProperties map
    * @param reader XMLStream reader
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    */
   protected void parseConfigProperty(Map<String, String> configProperties, XMLStreamReader reader)
      throws XMLStreamException,
      ParserException
   {
      String n = attributeAsString(reader, "name");
      if (n == null || n.trim().equals(""))
         throw new ParserException(bundle.requiredAttributeMissing("name", reader.getLocalName()));
      else
         configProperties.put(n, elementAsString(reader));
   }
   
   private static class SecurityActions
   {
      /**
       * Constructor
       */
      private SecurityActions()
      {
      }

      /**
       * Get a system property
       * @param name The property name
       * @return The property value
       */
      static String getSystemProperty(final String name)
      {
         if (System.getSecurityManager() == null)
         {
            return System.getProperty(name);
         }
         else
         {
            return (String) AccessController.doPrivileged(new PrivilegedAction<Object>()
            {
               public Object run()
               {
                  return System.getProperty(name);
               }
            });
         }
      }
   }
}
