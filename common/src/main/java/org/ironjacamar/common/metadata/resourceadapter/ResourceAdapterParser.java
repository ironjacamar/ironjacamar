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
package org.ironjacamar.common.metadata.resourceadapter;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.Activations;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.resourceadapter.WorkManager;
import org.ironjacamar.common.api.validator.ValidateException;
import org.ironjacamar.common.metadata.MetadataParser;
import org.ironjacamar.common.metadata.ParserException;
import org.ironjacamar.common.metadata.common.CommonIronJacamarParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 * A ResourceAdapterParserr.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ResourceAdapterParser extends CommonIronJacamarParser implements MetadataParser<Activations>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    * {@inheritDoc}
    */
   public Activations parse(XMLStreamReader reader) throws Exception
   {

      Activations adapters = null;

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
               case XML.ELEMENT_RESOURCE_ADAPTERS : {
                  adapters = parseResourceAdapters(reader);
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

      return adapters;

   }

   /**
    * Store a -ra.xml file
    * @param metadata The resource adapter definitions
    * @param writer The writer
    * @exception Exception Thrown if an error occurs
    */
   public void store(Activations metadata, XMLStreamWriter writer) throws Exception
   {
      if (metadata != null && writer != null)
      {
         writer.writeStartElement(XML.ELEMENT_RESOURCE_ADAPTERS);
         for (Activation a : metadata.getActivations())
         {
            writer.writeStartElement(XML.ELEMENT_RESOURCE_ADAPTER);

            if (a.getId() != null)
               writer.writeAttribute(XML.ATTRIBUTE_ID, a.getValue(XML.ATTRIBUTE_ID, a.getId()));

            writer.writeStartElement(XML.ELEMENT_ARCHIVE);
            writer.writeCharacters(a.getValue(XML.ELEMENT_ARCHIVE, a.getArchive()));
            writer.writeEndElement();

            storeCommon(a, writer);

            writer.writeEndElement();
         }
         writer.writeEndElement();
      }
   }

   private Activations parseResourceAdapters(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      ArrayList<Activation> resourceAdapters = new ArrayList<Activation>();
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_RESOURCE_ADAPTERS.equals(reader.getLocalName()))
               {
                  resourceAdapters.trimToSize();
                  return new ActivationsImpl(resourceAdapters);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_RESOURCE_ADAPTER :
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
                  case XML.ELEMENT_RESOURCE_ADAPTER : {
                     resourceAdapters.add(parseResourceAdapter(reader));
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

   private Activation parseResourceAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      List<ConnectionDefinition> connectionDefinitions = null;
      List<AdminObject> adminObjects = null;
      List<String> beanValidationGroups = null;
      String bootstrapContext = null;
      String id = null;
      String archive = null;
      TransactionSupportEnum transactionSupport = null;
      Map<String, String> configProperties = null;
      WorkManager workmanager = null;
      Boolean isXA = null;

      Map<String, String> expressions = new HashMap<String, String>();

      int attributeSize = reader.getAttributeCount();
      for (int i = 0; i < attributeSize; i++)
      {
         switch (reader.getAttributeLocalName(i))
         {
            case XML.ATTRIBUTE_ID : {
               id = attributeAsString(reader, XML.ATTRIBUTE_ID, expressions);
               break;
            }
            default :
               throw new ParserException(bundle.unexpectedAttribute(reader.getAttributeLocalName(i),
                                                                    reader.getLocalName()));
         }
      }

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_RESOURCE_ADAPTER.equals(reader.getLocalName()))
               {
                  return new ActivationImpl(id, archive, transactionSupport, connectionDefinitions, adminObjects,
                                            configProperties, beanValidationGroups, bootstrapContext, workmanager,
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
                     case XML.ELEMENT_BEAN_VALIDATION_GROUP :
                     case XML.ELEMENT_BOOTSTRAP_CONTEXT :
                     case XML.ELEMENT_CONFIG_PROPERTY :
                     case XML.ELEMENT_TRANSACTION_SUPPORT :
                     case XML.ELEMENT_ARCHIVE :
                     case XML.ELEMENT_WORKMANAGER :
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
                     parseConfigProperty(configProperties, reader, XML.ELEMENT_CONFIG_PROPERTY, expressions);
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_SUPPORT : {
                     transactionSupport =
                        TransactionSupportEnum.valueOf(elementAsString(reader, XML.ELEMENT_TRANSACTION_SUPPORT,
                                                                       expressions));

                     if (transactionSupport == TransactionSupportEnum.XATransaction)
                        isXA = Boolean.TRUE;

                     break;
                  }
                  case XML.ELEMENT_ARCHIVE : {
                     archive = elementAsString(reader, XML.ELEMENT_ARCHIVE, expressions);
                     break;
                  }
                  case XML.ELEMENT_WORKMANAGER : {
                     workmanager = parseWorkManager(reader);
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
