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
import org.ironjacamar.common.api.metadata.Defaults;
import org.ironjacamar.common.api.metadata.common.Pool;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.common.Security;
import org.ironjacamar.common.api.metadata.common.TimeOut;
import org.ironjacamar.common.api.metadata.common.Validation;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.resourceadapter.WorkManager;
import org.ironjacamar.common.api.metadata.resourceadapter.WorkManagerSecurity;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.ParserException;
import org.ironjacamar.common.metadata.resourceadapter.AdminObjectImpl;
import org.ironjacamar.common.metadata.resourceadapter.ConnectionDefinitionImpl;
import org.ironjacamar.common.metadata.resourceadapter.WorkManagerImpl;
import org.ironjacamar.common.metadata.resourceadapter.WorkManagerSecurityImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 * A CommonIronJacamarParser.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class CommonIronJacamarParser extends AbstractParser
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    *
    * parse a single admin-oject tag
    *
    * @param reader the reader
    * @return the parsed {@link AdminObject}
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    */
   protected AdminObject parseAdminObjects(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      Map<String, String> configProperties = new HashMap<String, String>();

      //attributes reading
      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String className = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      String poolName = null;

      int attributeSize = reader.getAttributeCount();

      for (int i = 0; i < attributeSize; i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_ENABLED, true);
               break;
            }
            case CommonXML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, CommonXML.ATTRIBUTE_JNDI_NAME);
               break;
            }
            case CommonXML.ATTRIBUTE_CLASS_NAME : {
               className = attributeAsString(reader, CommonXML.ATTRIBUTE_CLASS_NAME);
               break;
            }
            case CommonXML.ATTRIBUTE_USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_USE_JAVA_CONTEXT, true);
               break;
            }
            case CommonXML.ATTRIBUTE_POOL_NAME : {
               poolName = attributeAsString(reader, CommonXML.ATTRIBUTE_POOL_NAME);
               break;
            }
            default :
               throw new ParserException(bundle.unexpectedAttribute(reader.getAttributeLocalName(i),
                                                                    reader.getLocalName()));
         }
      }
      if (jndiName == null || jndiName.trim().equals(""))
         throw new ParserException(bundle.missingJndiName(reader.getLocalName()));

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonXML.ELEMENT_ADMIN_OBJECT.equals(reader.getLocalName()))
               {
                  return new AdminObjectImpl(configProperties, className, jndiName, poolName, enabled,
                                             useJavaContext);
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
                     parseConfigProperty(configProperties, reader);
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
    * Parse workmanager element
    * @param reader The reader
    * @return The value
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected WorkManager parseWorkManager(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      WorkManagerSecurity security = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonXML.ELEMENT_WORKMANAGER.equals(reader.getLocalName()))
               {
                  return new WorkManagerImpl(security);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_SECURITY :
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
                  case CommonXML.ELEMENT_SECURITY : {
                     security = parseWorkManagerSecurity(reader);
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
    * Parse workmanager's security element
    * @param reader The reader
    * @return The value
    * @exception XMLStreamException XMLStreamException
    * @exception ParserException ParserException
    * @exception ValidateException ValidateException
    */
   protected WorkManagerSecurity parseWorkManagerSecurity(XMLStreamReader reader) throws XMLStreamException,
      ParserException, ValidateException
   {
      boolean mappingRequired = false;
      String domain = null;
      String defaultPrincipal = null;
      List<String> defaultGroups = null;
      Map<String, String> userMappings = null;
      Map<String, String> groupMappings = null;

      boolean userMappingEnabled = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonXML.ELEMENT_WORKMANAGER_SECURITY.equals(reader.getLocalName()))
               {
                  return new WorkManagerSecurityImpl(mappingRequired, domain,
                                                     defaultPrincipal, defaultGroups,
                                                     userMappings, groupMappings);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_DEFAULT_GROUPS :
                     case CommonXML.ELEMENT_MAPPINGS :
                     case CommonXML.ELEMENT_MAPPING_REQUIRED :
                     case CommonXML.ELEMENT_DOMAIN :
                     case CommonXML.ELEMENT_DEFAULT_PRINCIPAL :
                     case CommonXML.ELEMENT_GROUP :
                     case CommonXML.ELEMENT_USERS :
                     case CommonXML.ELEMENT_GROUPS :
                     case CommonXML.ELEMENT_MAP :
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
                  case CommonXML.ELEMENT_DEFAULT_GROUPS :
                  case CommonXML.ELEMENT_MAPPINGS : {
                     // Skip
                     break;
                  }
                  case CommonXML.ELEMENT_MAPPING_REQUIRED : {
                     mappingRequired = elementAsBoolean(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_DOMAIN : {
                     domain = elementAsString(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_DEFAULT_PRINCIPAL : {
                     defaultPrincipal = elementAsString(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_GROUP : {
                     if (defaultGroups == null)
                        defaultGroups = new ArrayList<String>(1);

                     defaultGroups.add(elementAsString(reader));
                     break;
                  }
                  case CommonXML.ELEMENT_USERS : {
                     userMappingEnabled = true;
                     break;
                  }
                  case CommonXML.ELEMENT_GROUPS : {
                     userMappingEnabled = false;
                     break;
                  }
                  case CommonXML.ELEMENT_MAP : {
                     if (userMappingEnabled)
                     {
                        if (userMappings == null)
                           userMappings = new HashMap<String, String>();

                        String from = attributeAsString(reader, CommonXML.ATTRIBUTE_FROM);

                        if (from == null || from.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(CommonXML.ATTRIBUTE_FROM,
                                                              reader.getLocalName()));

                        String to = attributeAsString(reader, CommonXML.ATTRIBUTE_TO);

                        if (to == null || to.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(CommonXML.ATTRIBUTE_TO,
                                                              reader.getLocalName()));

                        userMappings.put(from, to);
                     }
                     else
                     {
                        if (groupMappings == null)
                           groupMappings = new HashMap<String, String>();

                        String from = attributeAsString(reader, CommonXML.ATTRIBUTE_FROM);

                        if (from == null || from.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(CommonXML.ATTRIBUTE_FROM,
                                                              reader.getLocalName()));

                        String to = attributeAsString(reader, CommonXML.ATTRIBUTE_TO);

                        if (to == null || to.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(CommonXML.ATTRIBUTE_TO,
                                                              reader.getLocalName()));

                        groupMappings.put(from, to);
                     }
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


   // v12

   /**
    *
    * parse a single connection-definition tag
    *
    * @param reader the reader
    * @param isXA Is XA capable
    * @return the parse {@link ConnectionDefinition} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected ConnectionDefinition parseConnectionDefinitions(XMLStreamReader reader, Boolean isXA)
      throws XMLStreamException, ParserException, ValidateException
   {
      Map<String, String> configProperties = new HashMap<String, String>();
      Security security = null;
      TimeOut timeOut = null;
      Validation validation = null;
      Pool pool = null;
      Recovery recovery = null;

      //attributes reading
      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String className = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      String poolName = null;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean sharable = Defaults.SHARABLE;
      Boolean enlistment = Defaults.ENLISTMENT;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;
      int attributeSize = reader.getAttributeCount();

      if (isXA == null)
         isXA = Boolean.FALSE;

      for (int i = 0; i < attributeSize; i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_ENABLED, Defaults.ENABLED);
               break;
            }
            case CommonXML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, CommonXML.ATTRIBUTE_JNDI_NAME);
               break;
            }
            case CommonXML.ATTRIBUTE_CLASS_NAME : {
               className = attributeAsString(reader, CommonXML.ATTRIBUTE_CLASS_NAME);
               break;
            }
            case CommonXML.ATTRIBUTE_POOL_NAME : {
               poolName = attributeAsString(reader, CommonXML.ATTRIBUTE_POOL_NAME);
               break;
            }
            case CommonXML.ATTRIBUTE_USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_USE_JAVA_CONTEXT,
                                                   Defaults.USE_JAVA_CONTEXT);
               break;
            }
            case CommonXML.ATTRIBUTE_USE_CCM : {
               useCcm = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_USE_CCM, Defaults.USE_CCM);
               break;
            }
            case CommonXML.ATTRIBUTE_SHARABLE : {
               sharable = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_SHARABLE, Defaults.SHARABLE);
               break;
            }
            case CommonXML.ATTRIBUTE_ENLISTMENT : {
               enlistment = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_ENLISTMENT, Defaults.ENLISTMENT);
               break;
            }
            case CommonXML.ATTRIBUTE_CONNECTABLE : {
               connectable = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_CONNECTABLE, Defaults.CONNECTABLE);
               break;
            }
            case CommonXML.ATTRIBUTE_TRACKING : {
               tracking = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_TRACKING, Defaults.TRACKING);
               break;
            }
            default :
               throw new ParserException(bundle.unexpectedAttribute(reader.getAttributeLocalName(i),
                                                                    reader.getLocalName()));
         }
      }
      if (jndiName == null || jndiName.trim().equals(""))
         throw new ParserException(bundle.missingJndiName(reader.getLocalName()));

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonXML.ELEMENT_CONNECTION_DEFINITION.equals(reader.getLocalName()))
               {
                  return new ConnectionDefinitionImpl(configProperties, className, jndiName, poolName, enabled,
                                                      useJavaContext, useCcm, sharable, enlistment,
                                                      connectable, tracking,
                                                      pool, timeOut, validation,
                                                      security, recovery, isXA);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case CommonXML.ELEMENT_CONFIG_PROPERTY :
                     case CommonXML.ELEMENT_SECURITY :
                     case CommonXML.ELEMENT_TIMEOUT :
                     case CommonXML.ELEMENT_VALIDATION :
                     case CommonXML.ELEMENT_XA_POOL :
                     case CommonXML.ELEMENT_POOL :
                     case CommonXML.ELEMENT_RECOVERY :
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
                     parseConfigProperty(configProperties, reader);
                     break;
                  }
                  case CommonXML.ELEMENT_SECURITY : {
                     security = parseSecuritySettings(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_TIMEOUT : {
                     timeOut = parseTimeOut(reader, isXA);
                     break;
                  }
                  case CommonXML.ELEMENT_VALIDATION : {
                     validation = parseValidation(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_XA_POOL : {
                     if (pool != null)
                        throw new ParserException(bundle.multiplePools());
                     pool = parseXaPool(reader);
                     isXA = Boolean.TRUE;
                     break;
                  }
                  case CommonXML.ELEMENT_POOL : {
                     if (pool != null)
                        throw new ParserException(bundle.multiplePools());
                     pool = parsePool(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_RECOVERY : {
                     recovery = parseRecovery(reader);
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
}
