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

import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonXaPool;
import org.jboss.jca.common.api.metadata.ds.DataSource;
import org.jboss.jca.common.api.metadata.ds.XaDataSource;
import org.jboss.jca.common.metadata.common.CommonPoolImpl;
import org.jboss.jca.common.metadata.common.CommonSecurityImpl;
import org.jboss.jca.common.metadata.common.CommonXaPoolImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

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

   /**
    *
    * parse a {@link CommonPool} object
    *
    * @param reader reader
    * @return the parsed {@link CommonPool} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    */
   protected CommonPool parsePool(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      boolean prefill = true;
      boolean useStrictMin = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.POOL)
               {

                  return new CommonPoolImpl(minPoolSize, maxPoolSize, prefill, useStrictMin);

               }
               else
               {
                  if (CommonPool.Tag.forName(reader.getLocalName()) == CommonPool.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonPool.Tag.forName(reader.getLocalName()))
               {
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }

                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
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
    * parse a {@link CommonSecurity} element
    *
    * @param reader reader
    * @return a {@link CommonSecurity} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    */
   protected CommonSecurity parseSecuritySettings(XMLStreamReader reader) throws XMLStreamException, ParserException
   {

      String userName = null;
      String password = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (DataSource.Tag.forName(reader.getLocalName()) == DataSource.Tag.SECURITY)
               {

                  return new CommonSecurityImpl(userName, password);
               }
               else
               {
                  if (CommonSecurity.Tag.forName(reader.getLocalName()) == CommonSecurity.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonSecurity.Tag.forName(reader.getLocalName()))
               {
                  case PASSWORD : {
                     password = elementAsString(reader);
                     break;
                  }
                  case USERNAME : {
                     userName = elementAsString(reader);
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
   * parse a {@link CommonXaPool} object
   *
   * @param reader reader
   * @return the parsed {@link CommonXaPool} object
   * @throws XMLStreamException XMLStreamException
   * @throws ParserException ParserException
   */
   protected CommonXaPool parseXaPool(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Integer minPoolSize = null;
      Integer maxPoolSize = null;
      boolean prefill = false;
      boolean interleaving = false;
      boolean isSameRmOverrideValue = false;
      boolean padXid = false;
      boolean noTxSeparatePool = false;
      boolean wrapXaDataSource = false;
      boolean useStrictMin = false;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XaDataSource.Tag.forName(reader.getLocalName()) == XaDataSource.Tag.XA_POOL)
               {

                  return new CommonXaPoolImpl(minPoolSize, maxPoolSize, prefill, useStrictMin, isSameRmOverrideValue,
                                        interleaving, padXid, wrapXaDataSource, noTxSeparatePool);

               }
               else
               {
                  if (CommonXaPool.Tag.forName(reader.getLocalName()) == CommonXaPool.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonXaPool.Tag.forName(reader.getLocalName()))
               {
                  case MAXPOOLSIZE : {
                     maxPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case MIN_POOL_SIZE : {
                     minPoolSize = elementAsInteger(reader);
                     break;
                  }
                  case INTERLEAVING : {
                     interleaving = elementAsBoolean(reader);
                     break;
                  }
                  case ISSAMERMOVERRIDEVALUE : {
                     isSameRmOverrideValue = elementAsBoolean(reader);
                     break;
                  }
                  case NO_TX_SEPARATE_POOLS : {
                     noTxSeparatePool = elementAsBoolean(reader);
                     break;
                  }
                  case PAD_XID : {
                     padXid = elementAsBoolean(reader);
                     break;
                  }
                  case WRAP_XA_RESOURCE : {
                     wrapXaDataSource = elementAsBoolean(reader);
                     break;
                  }
                  case PREFILL : {
                     prefill = elementAsBoolean(reader);
                     break;
                  }
                  case USE_STRICT_MIN : {
                     useStrictMin = elementAsBoolean(reader);
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

}
