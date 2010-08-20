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
package org.jboss.jca.common.metadata;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * A AbstractParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class AbstractParser
{

   /**
    * convert an xml element in boolean value. Empty elements results with true (tag presence is sufficient condition)
    *
    * @param reader the StAX reader
    * @return the boolean representing element
    * @throws XMLStreamException StAX exception
    */
   protected boolean elementAsBoolean(XMLStreamReader reader) throws XMLStreamException
   {
      String elementtext = reader.getElementText();
      return elementtext == null || elementtext.length() == 0 ? true : Boolean.valueOf(elementtext.trim());
   }

   /**
    * convert an xml attribute in boolean value. Empty elements results with false
    *
    * @param reader the StAX reader
    * @param attributeName the name of the attribute
    * @param defaultValue  defaultValue
    * @return the boolean representing element
    * @throws XMLStreamException StAX exception
    */
   protected boolean attributeAsBoolean(XMLStreamReader reader, String attributeName, boolean defaultValue)
      throws XMLStreamException
   {
      return reader.getAttributeValue("", attributeName) == null
            || reader.getAttributeValue("", attributeName).length() == 0
            ? defaultValue :
            Boolean.valueOf(reader.getAttributeValue("", attributeName).trim());
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
      String elementtext = reader.getElementText();
      return elementtext == null ? null : elementtext.trim();
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
      return reader.getAttributeValue("", attributeName) == null ? null : reader.getAttributeValue("", attributeName)
            .trim();
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
      try
      {

         integerValue = Integer.valueOf(elementAsString(reader));
      }
      catch (NumberFormatException nfe)
      {
         throw new ParserException(reader.getLocalName() + "isn't a valid number");
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
      try
      {
         longValue = Long.valueOf(elementAsString(reader));
      }
      catch (NumberFormatException nfe)
      {
         throw new ParserException(reader.getLocalName() + "isn't a valid number");
      }
      return longValue;
   }

}
