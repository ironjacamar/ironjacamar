/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.metadata.common;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.CommonLogger;
import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Capacity;
import org.ironjacamar.common.api.metadata.common.Credential;
import org.ironjacamar.common.api.metadata.common.Extension;
import org.ironjacamar.common.api.metadata.common.FlushStrategy;
import org.ironjacamar.common.api.metadata.common.Pool;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.common.Security;
import org.ironjacamar.common.api.metadata.common.TimeOut;
import org.ironjacamar.common.api.metadata.common.Validation;
import org.ironjacamar.common.api.metadata.common.XaPool;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.ParserException;

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
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      Boolean useStrictMin = Defaults.USE_STRICT_MIN;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonXML.ELEMENT_POOL.equals(reader.getLocalName()))
               {
                  return new PoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
                                      flushStrategy, capacity);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_MAX_POOL_SIZE :
                     case CommonXML.ELEMENT_MIN_POOL_SIZE :
                     case CommonXML.ELEMENT_INITIAL_POOL_SIZE :
                     case CommonXML.ELEMENT_PREFILL :
                     case CommonXML.ELEMENT_USE_STRICT_MIN :
                     case CommonXML.ELEMENT_FLUSH_STRATEGY :
                     case CommonXML.ELEMENT_CAPACITY :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_CAPACITY : {
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
               if (CommonXML.ELEMENT_XA_POOL.equals(reader.getLocalName()))
               {

                  return new XaPoolImpl(minPoolSize, initialPoolSize, maxPoolSize, prefill, useStrictMin,
                                        flushStrategy, capacity,
                                        isSameRmOverride, interleaving, padXid,
                                        wrapXaDataSource, noTxSeparatePool);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_MAX_POOL_SIZE :
                     case CommonXML.ELEMENT_INITIAL_POOL_SIZE :
                     case CommonXML.ELEMENT_MIN_POOL_SIZE :
                     case CommonXML.ELEMENT_INTERLEAVING :
                     case CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE :
                     case CommonXML.ELEMENT_NO_TX_SEPARATE_POOLS :
                     case CommonXML.ELEMENT_PAD_XID :
                     case CommonXML.ELEMENT_WRAP_XA_RESOURCE :
                     case CommonXML.ELEMENT_PREFILL :
                     case CommonXML.ELEMENT_USE_STRICT_MIN :
                     case CommonXML.ELEMENT_FLUSH_STRATEGY :
                     case CommonXML.ELEMENT_CAPACITY :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_INTERLEAVING : {
                     interleaving = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE : {
                     isSameRmOverride = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_NO_TX_SEPARATE_POOLS : {
                     noTxSeparatePool = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_PAD_XID : {
                     padXid = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_CAPACITY : {
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
               if (CommonXML.ELEMENT_SECURITY.equals(reader.getLocalName()))
               {
                  return new SecurityImpl(securityDomain, securityDomainAndApplication,
                                          application);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_SECURITY_DOMAIN :
                     case CommonXML.ELEMENT_SECURITY_DOMAIN_AND_APPLICATION :
                     case CommonXML.ELEMENT_APPLICATION :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {

                  case CommonXML.ELEMENT_SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_SECURITY_DOMAIN_AND_APPLICATION : {
                     securityDomainAndApplication = elementAsString(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_APPLICATION : {
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
               if (CommonXML.ELEMENT_SECURITY.equals(reader.getLocalName()) ||
                   CommonXML.ELEMENT_RECOVER_CREDENTIAL.equals(reader.getLocalName()))
               {
                  return new CredentialImpl(userName, password, securityDomain);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_PASSWORD :
                     case CommonXML.ELEMENT_USER_NAME :
                     case CommonXML.ELEMENT_SECURITY_DOMAIN :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_USER_NAME : {
                     userName = elementAsString(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_SECURITY_DOMAIN : {
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
      Boolean noRecovery = Defaults.NO_RECOVERY;
      Credential security = null;
      Extension plugin = null;

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_NO_RECOVERY : {
               noRecovery = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_NO_RECOVERY, Boolean.FALSE);
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
               if (CommonXML.ELEMENT_RECOVERY.equals(reader.getLocalName()))
               {
                  return new Recovery(security, plugin, noRecovery);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_RECOVER_CREDENTIAL :
                     case CommonXML.ELEMENT_RECOVER_PLUGIN :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_RECOVER_CREDENTIAL : {
                     security = parseCredential(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_RECOVER_PLUGIN : {
                     plugin = parseExtension(reader, CommonXML.ELEMENT_RECOVER_PLUGIN);
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

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_CLASS_NAME : {
               className = attributeAsString(reader, CommonXML.ATTRIBUTE_CLASS_NAME);
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

                  return new Extension(className, properties);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_CONFIG_PROPERTY :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_CONFIG_PROPERTY : {
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
               if (CommonXML.ELEMENT_CAPACITY.equals(reader.getLocalName()))
               {
                  return new Capacity(incrementer, decrementer);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_INCREMENTER :
                     case CommonXML.ELEMENT_DECREMENTER :
                        break;
                     default :
                        // Nothing
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_INCREMENTER : {
                     incrementer = parseExtension(reader, CommonXML.ELEMENT_INCREMENTER);
                     break;
                  }
                  case CommonXML.ELEMENT_DECREMENTER : {
                     decrementer = parseExtension(reader, CommonXML.ELEMENT_DECREMENTER);
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
               if (CommonXML.ELEMENT_VALIDATION.equals(reader.getLocalName()))
               {
                  return new ValidationImpl(validateOnMatch, backgroundValidation, backgroundValidationMillis,
                                            useFastFail);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_VALIDATE_ON_MATCH :
                     case CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS :
                     case CommonXML.ELEMENT_BACKGROUND_VALIDATION :
                     case CommonXML.ELEMENT_USE_FAST_FAIL :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_USE_FAST_FAIL : {
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
               if (CommonXML.ELEMENT_TIMEOUT.equals(reader.getLocalName()))
               {
                  return new TimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                         allocationRetryWaitMillis, xaResourceTimeout);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS :
                     case CommonXML.ELEMENT_ALLOCATION_RETRY :
                     case CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS :
                     case CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES :
                     case CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT : {
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
