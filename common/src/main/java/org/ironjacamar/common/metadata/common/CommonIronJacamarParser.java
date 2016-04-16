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
import org.ironjacamar.common.api.metadata.common.Timeout;
import org.ironjacamar.common.api.metadata.common.Validation;
import org.ironjacamar.common.api.metadata.common.XaPool;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.logging.Messages;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

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
      Map<String, String> configProperties = new TreeMap<String, String>();

      //attributes reading
      String className = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      String id = null;

      HashMap<String, String> expressions = new HashMap<String, String>();

      int attributeSize = reader.getAttributeCount();

      for (int i = 0; i < attributeSize; i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_ENABLED, true, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, CommonXML.ATTRIBUTE_JNDI_NAME, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_CLASS_NAME : {
               className = attributeAsString(reader, CommonXML.ATTRIBUTE_CLASS_NAME, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_ID : {
               id = attributeAsString(reader, CommonXML.ATTRIBUTE_ID, expressions);
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
                  return new AdminObjectImpl(configProperties, className, jndiName, id, enabled,
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
                     parseConfigProperty(configProperties, reader, CommonXML.ELEMENT_CONFIG_PROPERTY, expressions);
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

      HashMap<String, String> expressions = new HashMap<String, String>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonXML.ELEMENT_WORKMANAGER_SECURITY.equals(reader.getLocalName()))
               {
                  return new WorkManagerSecurityImpl(mappingRequired, domain,
                                                     defaultPrincipal, defaultGroups,
                                                     userMappings, groupMappings,
                          !expressions.isEmpty() ? expressions : null);
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
                     mappingRequired = elementAsBoolean(reader, CommonXML.ELEMENT_MAPPING_REQUIRED, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_DOMAIN : {
                     domain = elementAsString(reader, CommonXML.ELEMENT_DOMAIN, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_DEFAULT_PRINCIPAL : {
                     defaultPrincipal = elementAsString(reader, CommonXML.ELEMENT_DEFAULT_PRINCIPAL, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_GROUP : {
                     if (defaultGroups == null)
                        defaultGroups = new ArrayList<String>(1);

                     defaultGroups.add(elementAsString(reader,
                                                       getExpressionKey(CommonXML.ELEMENT_GROUP,
                                                                        Integer.toString(defaultGroups.size())),
                                                       expressions));
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

                        String from = attributeAsString(reader, CommonXML.ATTRIBUTE_FROM, expressions);

                        if (from == null || from.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(CommonXML.ATTRIBUTE_FROM,
                                                              reader.getLocalName()));

                        String to = attributeAsString(reader, CommonXML.ATTRIBUTE_TO, expressions);

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

                        String from = attributeAsString(reader, CommonXML.ATTRIBUTE_FROM, expressions);

                        if (from == null || from.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(CommonXML.ATTRIBUTE_FROM,
                                                              reader.getLocalName()));

                        String to = attributeAsString(reader, CommonXML.ATTRIBUTE_TO, expressions);

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
      Map<String, String> configProperties = new TreeMap<String, String>();
      Security security = null;
      Timeout timeout = null;
      Validation validation = null;
      Pool pool = null;
      Recovery recovery = null;

      //attributes reading
      String className = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      String id = null;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean sharable = Defaults.SHARABLE;
      Boolean enlistment = Defaults.ENLISTMENT;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;
      int attributeSize = reader.getAttributeCount();

      HashMap<String, String> expressions = new HashMap<String, String>();

      if (isXA == null)
         isXA = Boolean.FALSE;

      for (int i = 0; i < attributeSize; i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case CommonXML.ATTRIBUTE_ENABLED : {
               enabled = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_ENABLED, Defaults.ENABLED, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_JNDI_NAME : {
               jndiName = attributeAsString(reader, CommonXML.ATTRIBUTE_JNDI_NAME, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_CLASS_NAME : {
               className = attributeAsString(reader, CommonXML.ATTRIBUTE_CLASS_NAME, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_ID : {
               id = attributeAsString(reader, CommonXML.ATTRIBUTE_ID, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_USE_CCM : {
               useCcm = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_USE_CCM, Defaults.USE_CCM, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_SHARABLE : {
               sharable = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_SHARABLE, Defaults.SHARABLE, expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_ENLISTMENT : {
               enlistment = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_ENLISTMENT, Defaults.ENLISTMENT,
                                               expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_CONNECTABLE : {
               connectable = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_CONNECTABLE, Defaults.CONNECTABLE,
                                                expressions);
               break;
            }
            case CommonXML.ATTRIBUTE_TRACKING : {
               tracking = attributeAsBoolean(reader, CommonXML.ATTRIBUTE_TRACKING, Defaults.TRACKING, expressions);
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
                  return new ConnectionDefinitionImpl(configProperties, className, jndiName, id, enabled,
                                                      useCcm, sharable, enlistment,
                                                      connectable, tracking,
                                                      pool, timeout, validation,
                                                      security, recovery, isXA,
                          !expressions.isEmpty() ? expressions : null);
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
                     parseConfigProperty(configProperties, reader, CommonXML.ELEMENT_CONFIG_PROPERTY, expressions);
                     break;
                  }
                  case CommonXML.ELEMENT_SECURITY : {
                     security = parseSecuritySettings(reader);
                     break;
                  }
                  case CommonXML.ELEMENT_TIMEOUT : {
                     timeout = parseTimeout(reader, isXA);
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

   /**
    * Store common
    * @param a The activation
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeCommon(Activation a, XMLStreamWriter writer) throws Exception
   {
      if (a.getBeanValidationGroups() != null && !a.getBeanValidationGroups().isEmpty())
      {
         writer.writeStartElement(CommonXML.ELEMENT_BEAN_VALIDATION_GROUPS);
         for (int i = 0; i < a.getBeanValidationGroups().size(); i++)
         {
            writer.writeStartElement(CommonXML.ELEMENT_BEAN_VALIDATION_GROUP);
            writer.writeCharacters(a.getValue(CommonXML.ELEMENT_BEAN_VALIDATION_GROUP, Integer.toString(i),
                                              a.getBeanValidationGroups().get(i)));
            writer.writeEndElement();
         }
         writer.writeEndElement();
      }

      if (a.getBootstrapContext() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_BOOTSTRAP_CONTEXT);
         writer.writeCharacters(a.getValue(CommonXML.ELEMENT_BOOTSTRAP_CONTEXT, a.getBootstrapContext()));
         writer.writeEndElement();
      }

      if (a.getConfigProperties() != null && !a.getConfigProperties().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = a.getConfigProperties().entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            writer.writeStartElement(CommonXML.ELEMENT_CONFIG_PROPERTY);
            writer.writeAttribute(CommonXML.ATTRIBUTE_NAME, entry.getKey());
            writer.writeCharacters(a.getValue(CommonXML.ELEMENT_CONFIG_PROPERTY, entry.getKey(), entry.getValue()));
            writer.writeEndElement();
         }
      }
      
      if (a.getTransactionSupport() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_TRANSACTION_SUPPORT);
         writer.writeCharacters(a.getValue(CommonXML.ELEMENT_TRANSACTION_SUPPORT,
                                           a.getTransactionSupport().toString()));
         writer.writeEndElement();
      }

      if (a.getWorkManager() != null)
         storeWorkManager(a.getWorkManager(), writer);

      if (a.getConnectionDefinitions() != null && !a.getConnectionDefinitions().isEmpty())
      {
         writer.writeStartElement(CommonXML.ELEMENT_CONNECTION_DEFINITIONS);
         for (ConnectionDefinition cd : a.getConnectionDefinitions())
         {
            storeConnectionDefinition(cd, writer);
         }
         writer.writeEndElement();
      }

      if (a.getAdminObjects() != null && !a.getAdminObjects().isEmpty())
      {
         writer.writeStartElement(CommonXML.ELEMENT_ADMIN_OBJECTS);
         for (AdminObject ao : a.getAdminObjects())
         {
            storeAdminObject(ao, writer);
         }
         writer.writeEndElement();
      }
   }

   /**
    * Store work manager
    * @param wm The work manager
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeWorkManager(WorkManager wm, XMLStreamWriter writer) throws Exception
   {
      WorkManagerSecurity s = wm.getSecurity();

      writer.writeStartElement(CommonXML.ELEMENT_WORKMANAGER);
      writer.writeStartElement(CommonXML.ELEMENT_WORKMANAGER_SECURITY);

      writer.writeStartElement(CommonXML.ELEMENT_MAPPING_REQUIRED);
      writer.writeCharacters(s.getValue(CommonXML.ELEMENT_MAPPING_REQUIRED, Boolean.toString(s.isMappingRequired())));
      writer.writeEndElement();

      writer.writeStartElement(CommonXML.ELEMENT_DOMAIN);
      writer.writeCharacters(s.getValue(CommonXML.ELEMENT_DOMAIN, s.getDomain()));
      writer.writeEndElement();

      if (s.getDefaultPrincipal() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_DEFAULT_PRINCIPAL);
         writer.writeCharacters(s.getValue(CommonXML.ELEMENT_DEFAULT_PRINCIPAL, s.getDefaultPrincipal()));
         writer.writeEndElement();
      }

      if (s.getDefaultGroups() != null && !s.getDefaultGroups().isEmpty())
      {
         writer.writeStartElement(CommonXML.ELEMENT_DEFAULT_GROUPS);
         for (int i = 0; i < s.getDefaultGroups().size(); i++)
         {
            writer.writeStartElement(CommonXML.ELEMENT_GROUP);
            writer.writeCharacters(s.getValue(CommonXML.ELEMENT_DEFAULT_GROUPS, Integer.toString(i),
                                              s.getDefaultGroups().get(i)));
            writer.writeEndElement();
         }
         writer.writeEndElement();
      }

      if ((s.getUserMappings() != null && !s.getUserMappings().isEmpty()) ||
          (s.getGroupMappings() != null && !s.getGroupMappings().isEmpty()))
      {
         writer.writeStartElement(CommonXML.ELEMENT_MAPPINGS);

         if (s.getUserMappings() != null && !s.getUserMappings().isEmpty())
         {
            writer.writeStartElement(CommonXML.ELEMENT_USERS);
            
            for (Map.Entry<String, String> entry : s.getUserMappings().entrySet())
            {
               writer.writeStartElement(CommonXML.ELEMENT_MAP);
               writer.writeAttribute(CommonXML.ATTRIBUTE_FROM,
                                     s.getValue(CommonXML.ELEMENT_USERS, entry.getKey(), entry.getKey()));
               writer.writeAttribute(CommonXML.ATTRIBUTE_TO,
                                     s.getValue(CommonXML.ELEMENT_USERS, entry.getValue(), entry.getValue()));
               writer.writeEndElement();
            }

            writer.writeEndElement();
         }

         if (s.getGroupMappings() != null && !s.getGroupMappings().isEmpty())
         {
            writer.writeStartElement(CommonXML.ELEMENT_GROUPS);
            
            for (Map.Entry<String, String> entry : s.getGroupMappings().entrySet())
            {
               writer.writeStartElement(CommonXML.ELEMENT_MAP);
               writer.writeAttribute(CommonXML.ATTRIBUTE_FROM,
                                     s.getValue(CommonXML.ELEMENT_GROUPS, entry.getKey(), entry.getKey()));
               writer.writeAttribute(CommonXML.ATTRIBUTE_TO,
                                     s.getValue(CommonXML.ELEMENT_GROUPS, entry.getValue(), entry.getValue()));
               writer.writeEndElement();
            }

            writer.writeEndElement();
         }

         writer.writeEndElement();
      }

      writer.writeEndElement();
      writer.writeEndElement();
   }

   /**
    * Store connection definition
    * @param cd The connection definition
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeConnectionDefinition(ConnectionDefinition cd, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_CONNECTION_DEFINITION);

      if (cd.getClassName() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_CLASS_NAME,
                               cd.getValue(CommonXML.ATTRIBUTE_CLASS_NAME, cd.getClassName()));

      if (cd.getJndiName() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_JNDI_NAME,
                               cd.getValue(CommonXML.ATTRIBUTE_JNDI_NAME, cd.getJndiName()));

      if (cd.isEnabled() != null && (cd.hasExpression(CommonXML.ATTRIBUTE_ENABLED) ||
                                     !Defaults.ENABLED.equals(cd.isEnabled())))
         writer.writeAttribute(CommonXML.ATTRIBUTE_ENABLED,
                               cd.getValue(CommonXML.ATTRIBUTE_ENABLED, cd.isEnabled().toString()));

      if (cd.getId() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_ID,
                               cd.getValue(CommonXML.ATTRIBUTE_ID, cd.getId()));

      if (cd.isUseCcm() != null && (cd.hasExpression(CommonXML.ATTRIBUTE_USE_CCM) ||
                                    !Defaults.USE_CCM.equals(cd.isUseCcm())))
         writer.writeAttribute(CommonXML.ATTRIBUTE_USE_CCM,
                               cd.getValue(CommonXML.ATTRIBUTE_USE_CCM, cd.isUseCcm().toString()));

      if (cd.isSharable() != null && (cd.hasExpression(CommonXML.ATTRIBUTE_SHARABLE) ||
                                      !Defaults.SHARABLE.equals(cd.isSharable())))
         writer.writeAttribute(CommonXML.ATTRIBUTE_SHARABLE,
                               cd.getValue(CommonXML.ATTRIBUTE_SHARABLE, cd.isSharable().toString()));

      if (cd.isEnlistment() != null && (cd.hasExpression(CommonXML.ATTRIBUTE_ENLISTMENT) ||
                                        !Defaults.ENLISTMENT.equals(cd.isEnlistment())))
         writer.writeAttribute(CommonXML.ATTRIBUTE_ENLISTMENT,
                               cd.getValue(CommonXML.ATTRIBUTE_ENLISTMENT, cd.isEnlistment().toString()));

      if (cd.isConnectable() != null && (cd.hasExpression(CommonXML.ATTRIBUTE_CONNECTABLE) ||
                                         !Defaults.CONNECTABLE.equals(cd.isConnectable())))
         writer.writeAttribute(CommonXML.ATTRIBUTE_CONNECTABLE,
                               cd.getValue(CommonXML.ATTRIBUTE_CONNECTABLE, cd.isConnectable().toString()));

      if (cd.isTracking() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_TRACKING,
                               cd.getValue(CommonXML.ATTRIBUTE_TRACKING, cd.isTracking().toString()));

      if (cd.getConfigProperties() != null && !cd.getConfigProperties().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = cd.getConfigProperties().entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            writer.writeStartElement(CommonXML.ELEMENT_CONFIG_PROPERTY);
            writer.writeAttribute(CommonXML.ATTRIBUTE_NAME, entry.getKey());
            writer.writeCharacters(cd.getValue(CommonXML.ELEMENT_CONFIG_PROPERTY, entry.getKey(), entry.getValue()));
            writer.writeEndElement();
         }
      }

      if (cd.getPool() != null)
      {
         if (cd.getPool() instanceof XaPool)
         {
            storeXaPool((XaPool)cd.getPool(), writer);
         }
         else
         {
            storePool(cd.getPool(), writer);
         }
      }

      if (cd.getSecurity() != null)
         storeSecurity(cd.getSecurity(), writer);

      if (cd.getTimeout() != null)
         storeTimeout(cd.getTimeout(), writer);

      if (cd.getValidation() != null)
         storeValidation(cd.getValidation(), writer);

      if (cd.getRecovery() != null)
         storeRecovery(cd.getRecovery(), writer);

      writer.writeEndElement();
   }

   /**
    * Store admin object
    * @param ao The admin object
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeAdminObject(AdminObject ao, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_ADMIN_OBJECT);

      if (ao.getClassName() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_CLASS_NAME,
                               ao.getValue(CommonXML.ATTRIBUTE_CLASS_NAME, ao.getClassName()));

      if (ao.getJndiName() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_JNDI_NAME,
                               ao.getValue(CommonXML.ATTRIBUTE_JNDI_NAME, ao.getJndiName()));

      if (ao.isEnabled() != null && (ao.hasExpression(CommonXML.ATTRIBUTE_ENABLED) ||
                                     !Defaults.ENABLED.equals(ao.isEnabled())))
         writer.writeAttribute(CommonXML.ATTRIBUTE_ENABLED,
               ao.getValue(CommonXML.ATTRIBUTE_ENABLED, ao.isEnabled().toString()));

      if (ao.getId() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_ID, ao.getValue(CommonXML.ATTRIBUTE_ID, ao.getId()));

      if (ao.getConfigProperties() != null && !ao.getConfigProperties().isEmpty())
      {
         Iterator<Map.Entry<String, String>> it = ao.getConfigProperties().entrySet().iterator();
         while (it.hasNext())
         {
            Map.Entry<String, String> entry = it.next();

            writer.writeStartElement(CommonXML.ELEMENT_CONFIG_PROPERTY);
            writer.writeAttribute(CommonXML.ATTRIBUTE_NAME, entry.getKey());
            writer.writeCharacters(ao.getValue(CommonXML.ELEMENT_CONFIG_PROPERTY, entry.getKey(), entry.getValue()));
            writer.writeEndElement();
         }
      }

      writer.writeEndElement();
   }

   /**
    * Store a pool
    * @param pool The pool
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storePool(Pool pool, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_POOL);

      if (pool.getType() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_TYPE,
                               pool.getValue(CommonXML.ATTRIBUTE_TYPE, pool.getType()));

      if (pool.getJanitor() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_JANITOR,
                               pool.getValue(CommonXML.ATTRIBUTE_JANITOR, pool.getJanitor()));

      if (pool.getMinPoolSize() != null && (pool.hasExpression(CommonXML.ELEMENT_MIN_POOL_SIZE) ||
                                            !Defaults.MIN_POOL_SIZE.equals(pool.getMinPoolSize())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_MIN_POOL_SIZE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_MIN_POOL_SIZE, pool.getMinPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getInitialPoolSize() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_INITIAL_POOL_SIZE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_INITIAL_POOL_SIZE,
                                              pool.getInitialPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getMaxPoolSize() != null && (pool.hasExpression(CommonXML.ELEMENT_MAX_POOL_SIZE) ||
                                            !Defaults.MAX_POOL_SIZE.equals(pool.getMaxPoolSize())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_MAX_POOL_SIZE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_MAX_POOL_SIZE, pool.getMaxPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.isPrefill() != null && (pool.hasExpression(CommonXML.ELEMENT_PREFILL) ||
                                       !Defaults.PREFILL.equals(pool.isPrefill())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_PREFILL);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_PREFILL, pool.isPrefill().toString()));
         writer.writeEndElement();
      }

      if (pool.getFlushStrategy() != null && (pool.hasExpression(CommonXML.ELEMENT_FLUSH_STRATEGY) ||
                                              !Defaults.FLUSH_STRATEGY.equals(pool.getFlushStrategy())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_FLUSH_STRATEGY);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_FLUSH_STRATEGY, pool.getFlushStrategy().toString()));
         writer.writeEndElement();
      }

      if (pool.getCapacity() != null)
         storeCapacity(pool.getCapacity(), writer);

      writer.writeEndElement();
   }

   /**
    * Store a XA pool
    * @param pool The pool
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeXaPool(XaPool pool, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_XA_POOL);

      if (pool.getType() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_TYPE,
                               pool.getValue(CommonXML.ATTRIBUTE_TYPE, pool.getType()));

      if (pool.getType() != null)
         writer.writeAttribute(CommonXML.ATTRIBUTE_JANITOR,
                               pool.getValue(CommonXML.ATTRIBUTE_JANITOR, pool.getJanitor()));

      if (pool.getMinPoolSize() != null && (pool.hasExpression(CommonXML.ELEMENT_MIN_POOL_SIZE) ||
                                            !Defaults.MIN_POOL_SIZE.equals(pool.getMinPoolSize())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_MIN_POOL_SIZE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_MIN_POOL_SIZE, pool.getMinPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getInitialPoolSize() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_INITIAL_POOL_SIZE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_INITIAL_POOL_SIZE,
                                              pool.getInitialPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.getMaxPoolSize() != null && (pool.hasExpression(CommonXML.ELEMENT_MAX_POOL_SIZE) ||
                                            !Defaults.MAX_POOL_SIZE.equals(pool.getMaxPoolSize())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_MAX_POOL_SIZE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_MAX_POOL_SIZE, pool.getMaxPoolSize().toString()));
         writer.writeEndElement();
      }

      if (pool.isPrefill() != null && (pool.hasExpression(CommonXML.ELEMENT_PREFILL) ||
                                       !Defaults.PREFILL.equals(pool.isPrefill())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_PREFILL);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_PREFILL, pool.isPrefill().toString()));
         writer.writeEndElement();
      }

      if (pool.getFlushStrategy() != null && (pool.hasExpression(CommonXML.ELEMENT_FLUSH_STRATEGY) ||
                                              !Defaults.FLUSH_STRATEGY.equals(pool.getFlushStrategy())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_FLUSH_STRATEGY);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_FLUSH_STRATEGY, pool.getFlushStrategy().toString()));
         writer.writeEndElement();
      }

      if (pool.getCapacity() != null)
         storeCapacity(pool.getCapacity(), writer);

      if (pool.isIsSameRmOverride() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_IS_SAME_RM_OVERRIDE,
                                              pool.isIsSameRmOverride().toString()));
         writer.writeEndElement();
      }

      if (pool.isPadXid() != null && (pool.hasExpression(CommonXML.ELEMENT_PAD_XID) ||
                                      !Defaults.PAD_XID.equals(pool.isPadXid())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_PAD_XID);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_PAD_XID, pool.isPadXid().toString()));
         writer.writeEndElement();
      }

      if (pool.isWrapXaResource() != null && (pool.hasExpression(CommonXML.ELEMENT_WRAP_XA_RESOURCE) ||
                                              !Defaults.WRAP_XA_RESOURCE.equals(pool.isWrapXaResource())))
      {
         writer.writeStartElement(CommonXML.ELEMENT_WRAP_XA_RESOURCE);
         writer.writeCharacters(pool.getValue(CommonXML.ELEMENT_WRAP_XA_RESOURCE, pool.isWrapXaResource().toString()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store security
    * @param s The security
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeSecurity(Security s, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_SECURITY);

      if (s.getSecurityDomain() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_SECURITY_DOMAIN);
         writer.writeCharacters(s.getValue(CommonXML.ELEMENT_SECURITY_DOMAIN, s.getSecurityDomain()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store validation
    * @param v The validation
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeValidation(Validation v, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_VALIDATION);

      if (v.isValidateOnMatch() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_VALIDATE_ON_MATCH);
         writer.writeCharacters(v.getValue(CommonXML.ELEMENT_VALIDATE_ON_MATCH, v.isValidateOnMatch().toString()));
         writer.writeEndElement();
      }

      if (v.isBackgroundValidation() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_BACKGROUND_VALIDATION);
         writer.writeCharacters(v.getValue(CommonXML.ELEMENT_BACKGROUND_VALIDATION,
                                           v.isBackgroundValidation().toString()));
         writer.writeEndElement();
      }

      if (v.getBackgroundValidationMillis() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS);
         writer.writeCharacters(v.getValue(CommonXML.ELEMENT_BACKGROUND_VALIDATION_MILLIS,
                                           v.getBackgroundValidationMillis().toString()));
         writer.writeEndElement();
      }

      if (v.isUseFastFail() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_USE_FAST_FAIL);
         writer.writeCharacters(v.getValue(CommonXML.ELEMENT_USE_FAST_FAIL, v.isUseFastFail().toString()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }

   /**
    * Store timeout
    * @param t The timeout
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   protected void storeTimeout(Timeout t, XMLStreamWriter writer) throws Exception
   {
      writer.writeStartElement(CommonXML.ELEMENT_TIMEOUT);

      if (t.getBlockingTimeoutMillis() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS);
         writer.writeCharacters(t.getValue(CommonXML.ELEMENT_BLOCKING_TIMEOUT_MILLIS,
                                           t.getBlockingTimeoutMillis().toString()));
         writer.writeEndElement();
      }

      if (t.getIdleTimeoutMinutes() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES);
         writer.writeCharacters(t.getValue(CommonXML.ELEMENT_IDLE_TIMEOUT_MINUTES,
                                           t.getIdleTimeoutMinutes().toString()));
         writer.writeEndElement();
      }

      if (t.getAllocationRetry() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_ALLOCATION_RETRY);
         writer.writeCharacters(t.getValue(CommonXML.ELEMENT_ALLOCATION_RETRY,
                                           t.getAllocationRetry().toString()));
         writer.writeEndElement();
      }

      if (t.getAllocationRetryWaitMillis() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS);
         writer.writeCharacters(t.getValue(CommonXML.ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS,
                                           t.getAllocationRetryWaitMillis().toString()));
         writer.writeEndElement();
      }

      if (t.getXaResourceTimeout() != null)
      {
         writer.writeStartElement(CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT);
         writer.writeCharacters(t.getValue(CommonXML.ELEMENT_XA_RESOURCE_TIMEOUT,
                                           t.getXaResourceTimeout().toString()));
         writer.writeEndElement();
      }

      writer.writeEndElement();
   }
}
