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
package org.jboss.jca.common.metadata.common.v11;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.v11.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.v11.WorkManager;
import org.jboss.jca.common.api.metadata.common.v11.WorkManagerSecurity;
import org.jboss.jca.common.api.metadata.ironjacamar.v11.IronJacamar;
import org.jboss.jca.common.api.metadata.resourceadapter.v11.ResourceAdapter;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ParserException;

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
 *
 * A CommonIronJacamarParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class CommonIronJacamarParser extends org.jboss.jca.common.metadata.common.v10.CommonIronJacamarParser
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    *
    * parse a single connection-definition tag
    *
    * @param reader the reader
    * @return the parse {@link CommonConnDef} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected CommonConnDef parseConnectionDefinitions(XMLStreamReader reader) throws XMLStreamException,
      ParserException, ValidateException
   {
      HashMap<String, String> configProperties = new HashMap<String, String>();
      CommonSecurity security = null;
      CommonTimeOut timeOut = null;
      CommonValidation validation = null;
      CommonPool pool = null;
      Recovery recovery = null;

      //attributes reading
      Boolean useJavaContext = Boolean.TRUE;
      String className = null;
      Boolean enabled = Boolean.TRUE;
      String jndiName = null;
      String poolName = null;
      Boolean useCcm = Boolean.TRUE;
      Boolean sharable = Boolean.TRUE;
      Boolean enlistment = Boolean.TRUE;
      int attributeSize = reader.getAttributeCount();

      Boolean isXa = Boolean.FALSE;

      for (int i = 0; i < attributeSize; i++)
      {
         CommonConnDef.Attribute attribute = CommonConnDef.Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case JNDI_NAME : {
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
            case USE_JAVA_CONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case USE_CCM : {
               useCcm = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case SHARABLE : {
               sharable = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case ENLISTMENT : {
               enlistment = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            default :
               throw new ParserException(bundle.unexpectedAttribute(attribute.getLocalName(), reader.getLocalName()));
         }
      }
      if (jndiName == null || jndiName.trim().equals(""))
         throw new ParserException(bundle.missingJndiName(reader.getLocalName()));

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.CONNECTION_DEFINITION)
               {

                  return new CommonConnDefImpl(configProperties, className, jndiName, poolName, enabled,
                                               useJavaContext, useCcm, sharable, enlistment,
                                               pool, timeOut, validation,
                                               security, recovery);
               }
               else
               {
                  if (CommonConnDef.Tag.forName(reader.getLocalName()) == CommonConnDef.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonConnDef.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     parseConfigProperty(configProperties, reader);
                     break;
                  }
                  case SECURITY : {
                     security = parseSecuritySettings(reader);
                     break;
                  }
                  case TIMEOUT : {
                     timeOut = parseTimeOut(reader, isXa);
                     break;
                  }
                  case VALIDATION : {
                     validation = parseValidation(reader);
                     break;
                  }
                  case XA_POOL : {
                     if (pool != null)
                        throw new ParserException(bundle.multiplePools());
                     pool = parseXaPool(reader);
                     isXa = true;
                     break;
                  }
                  case POOL : {
                     if (pool != null)
                        throw new ParserException(bundle.multiplePools());
                     pool = parsePool(reader);
                     break;
                  }
                  case RECOVERY : {
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
               if (IronJacamar.Tag.forName(reader.getLocalName()) == IronJacamar.Tag.WORKMANAGER)
               {
                  return new WorkManagerImpl(security);
               }
               else
               {
                  if (IronJacamar.Tag.forName(reader.getLocalName()) == IronJacamar.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (WorkManager.Tag.forName(reader.getLocalName()))
               {
                  case SECURITY : {
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
               if (WorkManager.Tag.forName(reader.getLocalName()) == WorkManager.Tag.SECURITY)
               {
                  return new WorkManagerSecurityImpl(mappingRequired, domain,
                                                     defaultPrincipal, defaultGroups,
                                                     userMappings, groupMappings);
               }
               else
               {
                  if (WorkManagerSecurity.Tag.forName(reader.getLocalName()) == WorkManagerSecurity.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (WorkManagerSecurity.Tag.forName(reader.getLocalName()))
               {
                  case DEFAULT_GROUPS :
                  case MAPPINGS : {
                     // Skip
                     break;
                  }
                  case MAPPING_REQUIRED : {
                     mappingRequired = elementAsBoolean(reader);
                     break;
                  }
                  case DOMAIN : {
                     domain = elementAsString(reader);
                     break;
                  }
                  case DEFAULT_PRINCIPAL : {
                     defaultPrincipal = elementAsString(reader);
                     break;
                  }
                  case GROUP : {
                     if (defaultGroups == null)
                        defaultGroups = new ArrayList<String>(1);

                     defaultGroups.add(elementAsString(reader));
                     break;
                  }
                  case USERS : {
                     userMappingEnabled = true;
                     break;
                  }
                  case GROUPS : {
                     userMappingEnabled = false;
                     break;
                  }
                  case MAP : {
                     if (userMappingEnabled)
                     {
                        if (userMappings == null)
                           userMappings = new HashMap<String, String>();

                        String from = attributeAsString(reader, WorkManagerSecurity.Attribute.FROM.getLocalName());

                        if (from == null || from.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(WorkManagerSecurity.Attribute.FROM.getLocalName(),
                                                              reader.getLocalName()));

                        String to = attributeAsString(reader, WorkManagerSecurity.Attribute.TO.getLocalName());

                        if (to == null || to.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(WorkManagerSecurity.Attribute.TO.getLocalName(),
                                                              reader.getLocalName()));

                        userMappings.put(from, to);
                     }
                     else
                     {
                        if (groupMappings == null)
                           groupMappings = new HashMap<String, String>();

                        String from = attributeAsString(reader, WorkManagerSecurity.Attribute.FROM.getLocalName());

                        if (from == null || from.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(WorkManagerSecurity.Attribute.FROM.getLocalName(),
                                                              reader.getLocalName()));

                        String to = attributeAsString(reader, WorkManagerSecurity.Attribute.TO.getLocalName());

                        if (to == null || to.trim().equals(""))
                           throw new ParserException(
                              bundle.requiredAttributeMissing(WorkManagerSecurity.Attribute.TO.getLocalName(),
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
}
