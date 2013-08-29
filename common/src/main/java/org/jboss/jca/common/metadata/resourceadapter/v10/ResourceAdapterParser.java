/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.common.metadata.resourceadapter.v10;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.common.v10.CommonConnDef;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapters;
import org.jboss.jca.common.api.metadata.resourceadapter.v10.ResourceAdapter;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.common.v10.CommonIronJacamarParser;
import org.jboss.jca.common.metadata.resourceadapter.ResourceAdaptersImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class ResourceAdapterParser extends CommonIronJacamarParser implements MetadataParser<ResourceAdapters>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   @Override
   public ResourceAdapters parse(InputStream xmlInputStream) throws Exception
   {

      XMLStreamReader reader = null;
      ResourceAdapters adapters = null;

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      try
      {
         return parse(reader);
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
   }

   @Override
   public ResourceAdapters parse(XMLStreamReader reader) throws Exception
   {

      ResourceAdapters adapters = null;

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
               case RESOURCE_ADAPTERS : {
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

   private ResourceAdapters parseResourceAdapters(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      ArrayList<org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter> resourceAdapters =
         new ArrayList<org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter>();
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.RESOURCE_ADAPTERS)
               {
                  resourceAdapters.trimToSize();
                  return new ResourceAdaptersImpl(resourceAdapters);
               }
               else
               {
                  if (ResourceAdapters.Tag.forName(reader.getLocalName()) == ResourceAdapters.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ResourceAdapters.Tag.forName(reader.getLocalName()))
               {
                  case RESOURCE_ADAPTER : {
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

   private org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter
   parseResourceAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException,
      ValidateException
   {
      List<CommonConnDef> connectionDefinitions = null;
      List<CommonAdminObject> adminObjects = null;
      List<String> beanValidationGroups = null;
      String bootstrapContext = null;
      String archive = null;
      TransactionSupportEnum transactionSupport = null;
      HashMap<String, String> configProperties = null;
      Boolean isXA = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapters.Tag.forName(reader.getLocalName()) == ResourceAdapters.Tag.RESOURCE_ADAPTER)
               {
                  return new ResourceAdapterImpl(archive, transactionSupport, connectionDefinitions, adminObjects,
                                                 configProperties, beanValidationGroups, bootstrapContext);
               }
               else
               {
                  if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (ResourceAdapter.Tag.forName(reader.getLocalName()))
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
                     connectionDefinitions.add(parseConnectionDefinitions(reader, isXA));
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
                     parseConfigProperty(configProperties, reader);
                     break;
                  }
                  case TRANSACTION_SUPPORT : {
                     transactionSupport = TransactionSupportEnum.valueOf(elementAsString(reader));

                     if (transactionSupport == TransactionSupportEnum.XATransaction)
                        isXA = Boolean.TRUE;

                     break;
                  }
                  case ARCHIVE : {
                     archive = elementAsString(reader);
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
   * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
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
      RESOURCE_ADAPTERS("resource-adapters");

      private String name;

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
       * Set the value
       * @param v The name
       * @return The value
       */
      Tag value(String v)
      {
         name = v;
         return this;
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
         return element == null ? UNKNOWN.value(localName) : element;
      }

   }

}
