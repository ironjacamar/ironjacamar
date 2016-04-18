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
package org.ironjacamar.common.metadata.ironjacamar;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.resourceadapter.WorkManager;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.MetadataParser;
import org.ironjacamar.common.metadata.ParserException;
import org.ironjacamar.common.metadata.common.CommonIronJacamarParser;
import org.ironjacamar.common.metadata.resourceadapter.ActivationImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 * An IronJacamarParserr.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class IronJacamarParser extends CommonIronJacamarParser implements MetadataParser<Activation>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    * {@inheritDoc}
    */
   public Activation parse(XMLStreamReader reader) throws Exception
   {
      Activation ironJacamar = null;

      try
      {
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

               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_IRONJACAMAR : {
                     ironJacamar = parseIronJacamar(reader);
                     break;
                  }
                  default :
                     throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
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
      return ironJacamar;

   }

   /**
    * Store an ironjacamar.xml file
    * @param metadata The IronJacamar definition
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   public void store(Activation metadata, XMLStreamWriter writer) throws Exception
   {
      if (metadata != null && writer != null)
      {
         writer.writeStartElement(XML.ELEMENT_IRONJACAMAR);
         storeCommon(metadata, writer);
         writer.writeEndElement();
      }
   }

   private Activation parseIronJacamar(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      ArrayList<ConnectionDefinition> connectionDefinitions = null;
      ArrayList<AdminObject> adminObjects = null;
      ArrayList<String> beanValidationGroups = null;
      String bootstrapContext = null;
      TransactionSupportEnum transactionSupport = null;
      Map<String, String> configProperties = null;
      WorkManager workManager = null;
      Boolean isXA = null;
      Map<String, String> expressions = new HashMap<String, String>();
      
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_IRONJACAMAR.equals(reader.getLocalName()))
               {
                  return new 
                     ActivationImpl(null, null, transactionSupport, connectionDefinitions, adminObjects,
                                    configProperties, beanValidationGroups, bootstrapContext, workManager,
                          !expressions.isEmpty() ? expressions : null);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_ADMIN_OBJECTS :
                     case XML.ELEMENT_CONNECTION_DEFINITIONS :
                     case XML.ELEMENT_BEAN_VALIDATION_GROUPS :
                     case XML.ELEMENT_ADMIN_OBJECT :
                     case XML.ELEMENT_CONNECTION_DEFINITION :
                     case XML.ELEMENT_WORKMANAGER :
                     case XML.ELEMENT_BEAN_VALIDATION_GROUP :
                     case XML.ELEMENT_BOOTSTRAP_CONTEXT :
                     case XML.ELEMENT_CONFIG_PROPERTY :
                     case XML.ELEMENT_TRANSACTION_SUPPORT :
                        break;
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (reader.getLocalName())
               {
                  case XML.ELEMENT_ADMIN_OBJECTS :
                  case XML.ELEMENT_CONNECTION_DEFINITIONS :
                  case XML.ELEMENT_BEAN_VALIDATION_GROUPS : {
                     //ignore it,we will parse bean-validation-group,admin_object and connection_definition directly
                     break;
                  }
                  case XML.ELEMENT_ADMIN_OBJECT : {
                     if (adminObjects == null)
                        adminObjects = new ArrayList<AdminObject>();
                     adminObjects.add(parseAdminObjects(reader));
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_DEFINITION : {
                     if (connectionDefinitions == null)
                        connectionDefinitions = new ArrayList<ConnectionDefinition>();
                     connectionDefinitions.add(parseConnectionDefinitions(reader, isXA));
                     break;
                  }
                  case XML.ELEMENT_WORKMANAGER : {
                     workManager = parseWorkManager(reader);
                     break;
                  }
                  case XML.ELEMENT_BEAN_VALIDATION_GROUP : {
                     if (beanValidationGroups == null)
                        beanValidationGroups = new ArrayList<String>();
                     beanValidationGroups.add(
                        elementAsString(reader,
                                        getExpressionKey(XML.ELEMENT_BEAN_VALIDATION_GROUP,
                                                         Integer.toString(beanValidationGroups.size())),
                                        expressions));
                     break;
                  }
                  case XML.ELEMENT_BOOTSTRAP_CONTEXT : {
                     bootstrapContext = elementAsString(reader, XML.ELEMENT_BOOTSTRAP_CONTEXT, expressions);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY : {
                     if (configProperties == null)
                        configProperties = new TreeMap<String, String>();
                     String n = attributeAsString(reader, XML.ATTRIBUTE_NAME, null);
                     if (n == null || n.trim().equals(""))
                        throw new ParserException(bundle.requiredAttributeMissing(XML.ATTRIBUTE_NAME,
                                                                                  reader.getLocalName()));
                     else
                        configProperties.put(n, elementAsString(reader,
                                                                getExpressionKey(XML.ELEMENT_CONFIG_PROPERTY,
                                                                                 n),
                                                                expressions));
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_SUPPORT : {
                     transactionSupport =
                        TransactionSupportEnum.valueOf(elementAsString(reader,
                                                                       XML.ELEMENT_TRANSACTION_SUPPORT,
                                                                       expressions));

                     if (transactionSupport == TransactionSupportEnum.XATransaction)
                        isXA = Boolean.TRUE;

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
