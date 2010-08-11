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
package org.jboss.jca.common.metadata.jbossra;

import org.jboss.jca.common.api.metadata.jbossra.JbossRa;
import org.jboss.jca.common.api.metadata.jbossra.jbossra10.JbossRa10;
import org.jboss.jca.common.api.metadata.jbossra.jbossra20.BeanValidationGroup;
import org.jboss.jca.common.api.metadata.jbossra.jbossra20.JbossRa20;
import org.jboss.jca.common.api.metadata.ra.OverrideElementAttribute;
import org.jboss.jca.common.api.metadata.ra.RaConfigProperty;
import org.jboss.jca.common.metadata.AbstractParser;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.jbossra.jbossra10.JbossRa10Impl;
import org.jboss.jca.common.metadata.jbossra.jbossra20.BeanValidationGroupImpl;
import org.jboss.jca.common.metadata.jbossra.jbossra20.JbossRa20Impl;
import org.jboss.jca.common.metadata.ra.common.RaConfigPropertyImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * A JbossRaParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class JbossRaParser extends AbstractParser implements MetadataParser<JbossRa>
{

   /**
    * Parse the xml file and return the {@link JbossRaAbstractImpl} metadata
    * @param xmlInputStream The stream on xml file to parse
    * @return The {@link JbossRaAbstractImpl} metadata
    * @exception Exception Thrown if an error occurs
    */
   @Override
   public JbossRa parse(InputStream xmlInputStream) throws Exception
   {
      XMLStreamReader reader = null;
      JbossRa jbossRa = null;

      try
      {
         XMLInputFactory inputFactory = XMLInputFactory.newInstance();
         reader = inputFactory.createXMLStreamReader(xmlInputStream);

         int iterate;
         try
         {
            iterate = reader.nextTag();
         }
         catch (XMLStreamException e)
         {
            //founding a non tag..go on Normally non-tag found at beginning are comments or DTD declaration
            iterate = reader.nextTag();
         }
         switch (iterate)
         {
            case END_ELEMENT : {
               // should mean we're done, so ignore it.
               break;
            }
            case START_ELEMENT : {
               if (JbossRa20.NAMESPACE.equals(reader.getNamespaceURI()))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case JBOSSRA : {
                        jbossRa = parseJbossRa20(reader);
                        break;
                     }
                     default :
                        throw new ParserException("Unexpected element:" + reader.getLocalName());
                  }
               }

               else
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case JBOSSRA : {
                        jbossRa = parseJbossRa10(reader);
                        break;
                     }
                     default :
                        throw new ParserException("Unexpected element:" + reader.getLocalName());
                  }

               }

               break;
            }
            default :
               throw new IllegalStateException();
         }
      }
      catch (XMLStreamException e)
      {
         //ignore it. It is just saying that it isn't a tag
      }
      finally
      {
         if (reader != null)
            reader.close();
      }
      return jbossRa;

   }

   private JbossRa20 parseJbossRa20(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<RaConfigProperty<?>> raConfigProperties = new ArrayList<RaConfigProperty<?>>();
      ArrayList<BeanValidationGroup> beanValidationGroups = new ArrayList<BeanValidationGroup>();
      String bootStrapContext = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.JBOSSRA)
               {
                  raConfigProperties.trimToSize();
                  beanValidationGroups.trimToSize();
                  return new JbossRa20Impl(raConfigProperties, bootStrapContext, beanValidationGroups);
               }
               else
               {
                  if (JbossRa10.Tag.forName(reader.getLocalName()) == JbossRa10.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (JbossRa20.Tag.forName(reader.getLocalName()))
               {
                  case RA_CONFIG_PROPERTY : {
                     raConfigProperties.add(parseConfigProperty(reader));
                     break;
                  }
                  case BOOTSTRAP_CONTEXT : {
                     bootStrapContext = reader.getElementText();
                     break;
                  }
                  case BEAN_VALIDATION_GROUPS : {
                     beanValidationGroups.add(parseBeanValidationGroups(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private JbossRa10 parseJbossRa10(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<RaConfigProperty<?>> raConfigProperties = new ArrayList<RaConfigProperty<?>>();
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.JBOSSRA)
               {
                  raConfigProperties.trimToSize();
                  return new JbossRa10Impl(raConfigProperties);
               }
               else
               {
                  if (JbossRa10.Tag.forName(reader.getLocalName()) == JbossRa10.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (JbossRa10.Tag.forName(reader.getLocalName()))
               {
                  case RA_CONFIG_PROPERTY : {
                     raConfigProperties.add(parseConfigProperty(reader));
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private RaConfigProperty<?> parseConfigProperty(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String value = null;
      String type = null;
      String name = null;
      //do it now because we are on right START_ELEMENT
      OverrideElementAttribute overrideElementAttribute = OverrideElementAttribute.forName(reader.getAttributeValue(0));
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (JbossRa10.Tag.forName(reader.getLocalName()) == JbossRa10.Tag.RA_CONFIG_PROPERTY
                     || JbossRa20.Tag.forName(reader.getLocalName()) == JbossRa20.Tag.RA_CONFIG_PROPERTY)
               {
                  return RaConfigPropertyImpl.buildRaConfigProperty(name, value, type, overrideElementAttribute);
               }
               else
               {
                  if (JbossRa10.Tag.forName(reader.getLocalName()) == JbossRa10.Tag.UNKNOWN
                        && JbossRa20.Tag.forName(reader.getLocalName()) == JbossRa20.Tag.UNKNOWN
                        && RaConfigProperty.Tag.forName(reader.getLocalName()) == RaConfigProperty.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (RaConfigProperty.Tag.forName(reader.getLocalName()))
               {
                  case RA_CONFIG_PROPERTY_NAME : {
                     name = reader.getElementText();
                     break;
                  }
                  case RA_CONFIG_PROPERTY_VALUE : {
                     value = reader.getElementText();
                     break;
                  }
                  case RA_CONFIG_PROPERTY_TYPE : {
                     type = reader.getElementText();
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
   }

   private BeanValidationGroupImpl parseBeanValidationGroups(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      ArrayList<String> beanValidationGroup = new ArrayList<String>();
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (JbossRa20.Tag.forName(reader.getLocalName()) == JbossRa20.Tag.BEAN_VALIDATION_GROUPS)
               {
                  beanValidationGroup.trimToSize();
                  return new BeanValidationGroupImpl(beanValidationGroup);
               }
               else
               {
                  if (JbossRa10.Tag.forName(reader.getLocalName()) == JbossRa10.Tag.UNKNOWN
                        && JbossRa20.Tag.forName(reader.getLocalName()) == JbossRa20.Tag.UNKNOWN
                        && BeanValidationGroup.Tag.forName(reader.getLocalName()) == BeanValidationGroup.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (BeanValidationGroup.Tag.forName(reader.getLocalName()))
               {
                  case BEAN_VALIDATION_GROUP : {
                     beanValidationGroup.add(reader.getElementText());
                     break;
                  }
                  default :
                     throw new ParserException("Unexpected element:" + reader.getLocalName());
               }
               break;
            }
         }
      }
      throw new ParserException("Reached end of xml document unexpectedly");
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
      JBOSSRA("jboss-ra");

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
