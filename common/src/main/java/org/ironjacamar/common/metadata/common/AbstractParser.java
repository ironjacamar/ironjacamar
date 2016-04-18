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
import org.ironjacamar.common.api.metadata.common.Timeout;
import org.ironjacamar.common.api.metadata.common.Validation;
import org.ironjacamar.common.api.metadata.common.XaPool;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.ParserException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.logging.Logger;
import org.jboss.logging.Messages;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

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
    * @param key The key
    * @param expressions The expressions
    * @return the boolean representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case of non valid boolean for given element value
    */
   protected Boolean elementAsBoolean(XMLStreamReader reader, String key, Map<String, String> expressions)
      throws XMLStreamException, ParserException
   {
      String elementtext = rawElementText(reader);

      if (key != null && expressions != null && elementtext != null && elementtext.indexOf("${") != -1)
         expressions.put(key, elementtext);
      
      String stringValue = getSubstitutionValue(elementtext);
      if (StringUtils.isEmpty(stringValue) || stringValue.trim().equalsIgnoreCase("true") ||
          stringValue.trim().equalsIgnoreCase("false"))
      {
         return StringUtils.isEmpty(stringValue) ? Boolean.TRUE : Boolean.valueOf(stringValue.trim());
      }
      else
      {
         throw new ParserException(bundle.elementAsBoolean(elementtext, reader.getLocalName()));
      }
   }

   /**
    * convert an xml attribute in boolean value. Empty elements results in default value
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @param defaultValue  defaultValue
    * @param expressions The expressions
    * @return the boolean representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case of not valid boolean for given attribute
    */
   protected Boolean attributeAsBoolean(XMLStreamReader reader, String attributeName, Boolean defaultValue,
                                        Map<String, String> expressions)
      throws XMLStreamException, ParserException
   {
      String attributeString = rawAttributeText(reader, attributeName);

      if (attributeName != null && expressions != null && attributeString != null &&
          attributeString.indexOf("${") != -1)
         expressions.put(attributeName, attributeString);
      
      String stringValue = getSubstitutionValue(attributeString);
      if (StringUtils.isEmpty(stringValue) || stringValue.trim().equalsIgnoreCase("true") ||
          stringValue.trim().equalsIgnoreCase("false"))
      {
         return StringUtils.isEmpty(stringValue) ? defaultValue : Boolean.valueOf(stringValue.trim());
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
    * @param key The key
    * @param expressions The expressions
    * @return the string representing element
    * @throws XMLStreamException StAX exception
    */
   protected String elementAsString(XMLStreamReader reader, String key, Map<String, String> expressions)
      throws XMLStreamException
   {
      String elementtext = rawElementText(reader);

      if (key != null && expressions != null && elementtext != null && elementtext.indexOf("${") != -1)
         expressions.put(key, elementtext);
      
      return getSubstitutionValue(elementtext);
   }

   /**
    * Read the raw element
    *
    * @param reader
    * @return the string representing the raw eleemnt text
    * @throws XMLStreamException
    */
   private String rawElementText(XMLStreamReader reader) throws XMLStreamException
   {
      String elementtext = reader.getElementText();

      if (elementtext == null)
         return null;

      return elementtext.trim();
   }

   /**
    * convert an xml element in String value
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @param expressions The expressions
    * @return the string representing element
    * @throws XMLStreamException StAX exception
    */
   protected String attributeAsString(XMLStreamReader reader, String attributeName, Map<String, String> expressions)
      throws XMLStreamException
   {
      String attributeString = rawAttributeText(reader, attributeName);

      if (attributeName != null && expressions != null && attributeString != null &&
          attributeString.indexOf("${") != -1)
         expressions.put(attributeName, attributeString);

      return getSubstitutionValue(attributeString);
   }

   /**
    * convert an xml element in String value
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @param expressions The expressions
    * @return the string representing element
    * @throws XMLStreamException StAX exception
    */
   protected Integer attributeAsInt(XMLStreamReader reader, String attributeName, Map<String, String> expressions)
      throws XMLStreamException
   {
      String attributeString = rawAttributeText(reader, attributeName);

      if (attributeName != null && expressions != null && attributeString != null &&
          attributeString.indexOf("${") != -1)
         expressions.put(attributeName, attributeString);

      return attributeString != null ? Integer.valueOf(getSubstitutionValue(attributeString)) : null;
   }

   /**
    * Read the raw attribute
    *
    * @param reader
    * @param attributeName
    * @return the string representing raw attribute textx
    */
   private String rawAttributeText(XMLStreamReader reader, String attributeName)
   {
      String attributeString = reader.getAttributeValue("", attributeName);

      if (attributeString == null)
         return null;

      return attributeString.trim();
   }

   /**
    * convert an xml element in Integer value
    *
    * @param reader the StAX reader
    * @param key The key
    * @param expressions The expressions
    * @return the integer representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case it isn't a number
    */
   protected Integer elementAsInteger(XMLStreamReader reader, String key, Map<String, String> expressions)
      throws XMLStreamException, ParserException
   {
      Integer integerValue = null;
      String elementtext = rawElementText(reader);

      if (key != null && expressions != null && elementtext != null && elementtext.indexOf("${") != -1)
         expressions.put(key, elementtext);
      
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
    * @param key The key
    * @param expressions The expressions
    * @return the long representing element
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case it isn't a number
    */
   protected Long elementAsLong(XMLStreamReader reader, String key, Map<String, String> expressions)
      throws XMLStreamException, ParserException
   {
      Long longValue = null;
      String elementtext = rawElementText(reader);

      if (key != null && expressions != null && elementtext != null && elementtext.indexOf("${") != -1)
         expressions.put(key, elementtext);
      
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
    * @param expressions expressions
    * @return the flush strategy represention
    * @throws XMLStreamException StAX exception
    * @throws ParserException in case it isn't a number
    */
   protected FlushStrategy elementAsFlushStrategy(XMLStreamReader reader, Map<String, String> expressions)
      throws XMLStreamException, ParserException
   {
      String elementtext = rawElementText(reader);

      if (expressions != null && elementtext != null && elementtext.indexOf("${") != -1)
         expressions.put(CommonXML.ELEMENT_FLUSH_STRATEGY, elementtext);
      
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
      String type = Defaults.TYPE;
      String janitor = Defaults.JANITOR;
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_TYPE : {
               type = attributeAsString(reader, CommonXML.ATTRIBUTE_TYPE, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_JANITOR : {
               janitor = attributeAsString(reader, CommonXML.ATTRIBUTE_JANITOR, expressions);
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
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_POOL :
                     return new PoolImpl(type, janitor, minPoolSize, initialPoolSize, maxPoolSize, prefill,
                           flushStrategy, capacity,
                             !expressions.isEmpty() ? expressions : null);
                  case CommonXML.ELEMENT_MAX_POOL_SIZE :
                  case CommonXML.ELEMENT_MIN_POOL_SIZE :
                  case CommonXML.ELEMENT_INITIAL_POOL_SIZE :
                  case CommonXML.ELEMENT_PREFILL :
                  case CommonXML.ELEMENT_FLUSH_STRATEGY :
                  case CommonXML.ELEMENT_CAPACITY :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader, CommonXML.ELEMENT_MAX_POOL_SIZE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader, CommonXML.ELEMENT_MIN_POOL_SIZE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader, CommonXML.ELEMENT_INITIAL_POOL_SIZE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader, CommonXML.ELEMENT_PREFILL, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader, expressions);
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
      String type = Defaults.TYPE;
      String janitor = Defaults.JANITOR;
      Integer minPoolSize = Defaults.MIN_POOL_SIZE;
      Integer initialPoolSize = Defaults.INITIAL_POOL_SIZE;
      Integer maxPoolSize = Defaults.MAX_POOL_SIZE;
      Boolean prefill = Defaults.PREFILL;
      FlushStrategy flushStrategy = Defaults.FLUSH_STRATEGY;
      Capacity capacity = null;
      Boolean isSameRmOverride = Defaults.IS_SAME_RM_OVERRIDE;
      Boolean padXid = Defaults.PAD_XID;
      Boolean wrapXaDataSource = Defaults.WRAP_XA_RESOURCE;

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_TYPE : {
               type = attributeAsString(reader, CommonXML.ATTRIBUTE_TYPE, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_JANITOR : {
               janitor = attributeAsString(reader, CommonXML.ATTRIBUTE_JANITOR, expressions);
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
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_XA_POOL :
                     return new XaPoolImpl(type, janitor, minPoolSize, initialPoolSize, maxPoolSize, prefill,
                           flushStrategy, capacity,
                           isSameRmOverride, padXid,
                           wrapXaDataSource,
                             !expressions.isEmpty() ? expressions : null);
                  case CommonXML.ELEMENT_MAX_POOL_SIZE :
                  case CommonXML.ELEMENT_INITIAL_POOL_SIZE :
                  case CommonXML.ELEMENT_MIN_POOL_SIZE :
                  case CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE :
                  case CommonXML.ELEMENT_PAD_XID :
                  case CommonXML.ELEMENT_WRAP_XA_RESOURCE :
                  case CommonXML.ELEMENT_PREFILL :
                  case CommonXML.ELEMENT_FLUSH_STRATEGY :
                  case CommonXML.ELEMENT_CAPACITY :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_MAX_POOL_SIZE : {
                     maxPoolSize = elementAsInteger(reader, CommonXML.ELEMENT_MAX_POOL_SIZE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_INITIAL_POOL_SIZE : {
                     initialPoolSize = elementAsInteger(reader, CommonXML.ELEMENT_INITIAL_POOL_SIZE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader, CommonXML.ELEMENT_MIN_POOL_SIZE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE : {
                     isSameRmOverride = elementAsBoolean(reader, CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_PAD_XID : {
                     padXid = elementAsBoolean(reader, CommonXML.ELEMENT_PAD_XID, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader, CommonXML.ELEMENT_WRAP_XA_RESOURCE, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_PREFILL : {
                     prefill = elementAsBoolean(reader, CommonXML.ELEMENT_PREFILL, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_FLUSH_STRATEGY : {
                     flushStrategy = elementAsFlushStrategy(reader, expressions);
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

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_SECURITY :
                     return new SecurityImpl(securityDomain,
                             !expressions.isEmpty() ? expressions : null);
                  case CommonXML.ELEMENT_SECURITY_DOMAIN :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {

                  case CommonXML.ELEMENT_SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader, CommonXML.ELEMENT_SECURITY_DOMAIN, expressions);
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
   private String getSubstitutionValue(String input)
   {
      if (!resolveSystemProperties)
         return input;
      return StringUtils.transformExpression(input);
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
      String securityDomain = null;

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_SECURITY :
                  case CommonXML.ELEMENT_RECOVERY_CREDENTIAL :
                     return new CredentialImpl(securityDomain,
                             !expressions.isEmpty() ? expressions : null);
                  case CommonXML.ELEMENT_SECURITY_DOMAIN :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_SECURITY_DOMAIN : {
                     securityDomain = elementAsString(reader, CommonXML.ELEMENT_SECURITY_DOMAIN, expressions);
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

      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_NO_RECOVERY : {
               noRecovery = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_NO_RECOVERY, Boolean.FALSE, expressions);
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
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_RECOVERY :
                     return new RecoveryImpl(security, plugin, noRecovery,
                             !expressions.isEmpty() ? expressions : null);
                  case CommonXML.ELEMENT_RECOVERY_CREDENTIAL :
                  case CommonXML.ELEMENT_RECOVERY_PLUGIN :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_RECOVERY_CREDENTIAL : {
                     security = parseCredential(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_RECOVERY_PLUGIN : {
                     plugin = parseExtension(reader, CommonXML.ELEMENT_RECOVERY_PLUGIN);
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
      String moduleName = null;
      String moduleSlot = null;
      Map<String, String> properties = null;
      Map<String, String> expressions = new HashMap<String, String>();

      for (int i = 0; i < reader.getAttributeCount(); i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_CLASS_NAME : {
               className = attributeAsString(reader, CommonXML.ATTRIBUTE_CLASS_NAME, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_MODULE_NAME : {
               moduleName = attributeAsString(reader, CommonXML.ATTRIBUTE_MODULE_NAME, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_MODULE_SLOT: {
               moduleSlot = attributeAsString(reader, CommonXML.ATTRIBUTE_MODULE_SLOT, expressions);
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

                  return new ExtensionImpl(className, moduleName, moduleSlot, properties,
                          !expressions.isEmpty() ? expressions : null);
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
                     parseConfigProperty(properties, reader, CommonXML.ELEMENT_CONFIG_PROPERTY, expressions);
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
    * @return the parsed capacity object
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
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_CAPACITY :
                     return new CapacityImpl(incrementer, decrementer);
                  default :
                     break;
               }
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

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_VALIDATION :
                     return new ValidationImpl(validateOnMatch, backgroundValidation, backgroundValidationMillis,
                        useFastFail,
                             !expressions.isEmpty() ? expressions : null);
                  case CommonXML.ELEMENT_VALIDATE_ON_MATCH :
                  case CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS :
                  case CommonXML.ELEMENT_BACKGROUND_VALIDATION :
                  case CommonXML.ELEMENT_USE_FAST_FAIL :
                     break;
                  default :
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
               }
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_VALIDATE_ON_MATCH : {
                     validateOnMatch = elementAsBoolean(reader, CommonXML.ELEMENT_VALIDATE_ON_MATCH, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS : {
                     backgroundValidationMillis = elementAsLong(reader, CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS,
                                                                expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_BACKGROUND_VALIDATION : {
                     backgroundValidation = elementAsBoolean(reader, CommonXML.ELEMENT_BACKGROUND_VALIDATION,
                                                             expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_USE_FAST_FAIL : {
                     useFastFail = elementAsBoolean(reader, CommonXML.ELEMENT_USE_FAST_FAIL, expressions);
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
   protected Timeout parseTimeout(XMLStreamReader reader, Boolean isXa) throws XMLStreamException,
      ParserException, ValidateException
   {
      Long blockingTimeoutMillis = null;
      Long allocationRetryWaitMillis = null;
      Integer idleTimeoutMinutes = null;
      Integer allocationRetry = null;
      Integer xaResourceTimeout = null;

      Map<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_TIMEOUT :
                     return new TimeoutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                           allocationRetryWaitMillis, xaResourceTimeout,
                             !expressions.isEmpty() ? expressions : null);
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
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader, CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS,
                                                               expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_ALLOCATION_RETRY : {
                     allocationRetry = elementAsInteger(reader, CommonXML.ELEMENT_ALLOCATION_RETRY, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader, CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS,
                                                           expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES : {
                     idleTimeoutMinutes = elementAsInteger(reader, CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT : {
                     if (isXa != null && Boolean.FALSE.equals(isXa))
                        throw new ParserException(bundle.unsupportedElement(reader.getLocalName()));
                     xaResourceTimeout = elementAsInteger(reader, CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT, expressions);
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
    * Store capacity
    * @param c The capacity
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeCapacity(Capacity c, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_CAPACITY);

      if (c.getIncrementer() != null)
      {
         storeExtension(c.getIncrementer(), writer, CommonXML.ELEMENT_INCREMENTER);
      }

      if (c.getDecrementer() != null)
      {
         storeExtension(c.getDecrementer(), writer, CommonXML.ELEMENT_DECREMENTER);
      }

      writer.writeEndElement();
   }
   /**
    * Store capacity
    * @param e The extension
    * @param writer The writer
    * @param elementName the element name
    * @exception XMLStreamException Thrown if an error occurs
    */
   protected void storeExtension(Extension e, XMLStreamWriter writer, String elementName) throws XMLStreamException
   {
      writer.writeStartElement(elementName);
      writer.writeAttribute(CommonXML.ATTRIBUTE_CLASS_NAME,
                            e.getValue(CommonXML.ATTRIBUTE_CLASS_NAME,
                                                        e.getClassName()));
      if (e.getModuleName() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_MODULE_NAME,
               e.getValue(CommonXML.ATTRIBUTE_MODULE_NAME, e.getModuleName()));
      if (e.getModuleSlot() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_MODULE_SLOT,
               e.getValue(CommonXML.ATTRIBUTE_MODULE_SLOT, e.getModuleSlot()));

      if (!e.getConfigPropertiesMap().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it =
            e.getConfigPropertiesMap().entrySet().iterator();

         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            writer.writeStartElement(CommonXML.ELEMENT_CONFIG_PROPERTY);
            writer.writeAttribute(CommonXML.ATTRIBUTE_NAME, entry.getKey());
            writer.writeCharacters(e.getValue(CommonXML.ELEMENT_CONFIG_PROPERTY,
                                                               entry.getKey(), entry.getValue()));
            writer.writeEndElement();
         }
      }

      writer.writeEndElement();
   }

   /**
    * Store recovery
    * @param r The recovery
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeRecovery(Recovery r, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_RECOVERY);

      if (r.isNoRecovery() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_NO_RECOVERY,
                               r.getValue(CommonXML.ATTRIBUTE_NO_RECOVERY, r.isNoRecovery().toString()));

      if (r.getCredential() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_RECOVERY_CREDENTIAL);

         writer.writeStartElement(CommonXML.ELEMENT_SECURITY_DOMAIN);
         writer.writeCharacters(r.getCredential().getValue(CommonXML.ELEMENT_SECURITY_DOMAIN,
                                                              r.getCredential().getSecurityDomain()));
         writer.writeEndElement();

         writer.writeEndElement();
      }

      if (r.getPlugin() != null)
      {
         storeExtension(r.getPlugin(), writer, CommonXML.ELEMENT_RECOVERY_PLUGIN);
      }

      writer.writeEndElement();
   }

   /**
    * 
    * Adds config property to the map
    * 
    * @param configProperties map
    * @param reader XMLStream reader
    * @param key The key
    * @param expressions expressions
    * @throws XMLStreamException in case of error
    * @throws ParserException in case of error
    */
   protected void parseConfigProperty(Map<String, String> configProperties, XMLStreamReader reader,
                                      String key, Map<String, String> expressions)
      throws XMLStreamException, ParserException
   {
      String n = attributeAsString(reader, "name", null);
      if (StringUtils.isEmptyTrimmed(n))
         throw new ParserException(bundle.requiredAttributeMissing("name", reader.getLocalName()));
      else
         configProperties.put(n, elementAsString(reader, getExpressionKey(key, n), expressions));
   }

   /**
    * Get expression key
    * @param k The key
    * @param s The subkey
    * @return The value
    */
   protected String getExpressionKey(String k, String s)
   {
      return k + "|" + s;
   }
}
