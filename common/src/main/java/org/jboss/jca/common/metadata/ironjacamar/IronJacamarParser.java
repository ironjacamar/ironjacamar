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
package org.jboss.jca.common.metadata.ironjacamar;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.common.CommonIronJacamarParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 *
 * A ResourceAdapterParserr.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class IronJacamarParser extends CommonIronJacamarParser implements MetadataParser<IronJacamar>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   @Override
   public IronJacamar parse(InputStream xmlInputStream) throws Exception
   {

      XMLStreamReader reader = null;
      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      return parse(reader);
   }

   @Override
   public IronJacamar parse(XMLStreamReader reader) throws Exception
   {

      IronJacamar ironJacamar = null;

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

               switch (Tag.forName(reader.getLocalName()))
               {
                  case IRONJACAMAR : {
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

   private IronJacamar parseIronJacamar(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      ArrayList<CommonConnDef> connectionDefinitions = null;
      ArrayList<CommonAdminObject> adminObjects = null;
      ArrayList<String> beanValidationGroups = null;
      String bootstrapContext = null;
      TransactionSupportEnum transactionSupport = null;
      HashMap<String, String> configProperties = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.IRONJACAMAR)
               {
                  return new IronJacamarImpl(transactionSupport, configProperties, adminObjects,
                                             connectionDefinitions, beanValidationGroups, bootstrapContext);
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
               switch (IronJacamar.Tag.forName(reader.getLocalName()))
               {
                  case ADMIN_OBJECTS :
                  case CONNECTION_DEFINITIONS :
                  case BEAN_VALIDATION_GROUPS : {
                     //ignore it,we will parse bean-validation-group,admin_object and connection_definition directly
                     break;
                  }
                  case ADMIN_OBJECT : {
                     if (adminObjects == null)
                        adminObjects = new ArrayList<CommonAdminObject>();
                     adminObjects.add(parseAdminObjects(reader));
                     break;
                  }

                  case CONNECTION_DEFINITION : {
                     if (connectionDefinitions == null)
                        connectionDefinitions = new ArrayList<CommonConnDef>();
                     connectionDefinitions.add(parseConnectionDefinitions(reader));
                     break;
                  }
                  case BEAN_VALIDATION_GROUP : {
                     if (beanValidationGroups == null)
                        beanValidationGroups = new ArrayList<String>();
                     beanValidationGroups.add(elementAsString(reader));
                     break;
                  }
                  case BOOTSTRAP_CONTEXT : {
                     bootstrapContext = elementAsString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     if (configProperties == null)
                        configProperties = new HashMap<String, String>();
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
                     break;

                  }
                  case TRANSACTION_SUPPORT : {
                     transactionSupport = TransactionSupportEnum.valueOf(elementAsString(reader));
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
      IRONJACAMAR("ironjacamar");

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
