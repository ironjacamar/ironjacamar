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
package org.jboss.jca.common.metadata.common;

import org.jboss.jca.common.api.metadata.common.CommonAdminObject;
import org.jboss.jca.common.api.metadata.common.CommonConnDef;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.resourceadapter.ResourceAdapter;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.AbstractParser;
import org.jboss.jca.common.metadata.ParserException;

import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 *
 * A CommonIronJacamarParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public abstract class CommonIronJacamarParser extends AbstractParser
{

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
      boolean useJavaContext = true;
      String className = null;
      boolean enabled = true;
      String jndiName = null;
      String poolName = null;
      int attributeSize = reader.getAttributeCount();

      boolean isXa = false;

      for (int i = 0; i < attributeSize; i++)
      {
         CommonConnDef.Attribute attribute = CommonConnDef.Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case JNDINAME : {
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
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            default :
               throw new ParserException("Unexpected attribute:" + attribute.getLocalName() + "at " +
                                         reader.getLocalName());
         }
      }
      if (jndiName == null || jndiName.trim().equals(""))
         throw new ParserException("Missing jndiName mandatory attribute");

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.CONNECTION_DEFINITION)
               {

                  return new CommonConnDefImpl(configProperties, className, jndiName, poolName, enabled,
                                               useJavaContext, pool, timeOut, validation, security, recovery);
               }
               else
               {
                  if (CommonConnDef.Tag.forName(reader.getLocalName()) == CommonConnDef.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonConnDef.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
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
                        throw new ParserException("You cannot define more than one pool or xa-poll in same"
                                                  + " connection-definition");
                     pool = parseXaPool(reader);
                     isXa = true;
                     break;
                  }
                  case POOL : {
                     if (pool != null)
                        throw new ParserException("You cannot define more than one pool or xa-poll in same"
                                                  + " connection-definition");
                     pool = parsePool(reader);
                     break;
                  }
                  case RECOVERY : {
                     recovery = parseRecovery(reader);
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

   private CommonValidation parseValidation(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      boolean useFastFail = false;
      boolean backgroundValidation = false;
      Long backgroundValidationMinutes = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonConnDef.Tag.forName(reader.getLocalName()) == CommonConnDef.Tag.VALIDATION)
               {

                  return new CommonValidationImpl(backgroundValidation, backgroundValidationMinutes, useFastFail);
               }
               else
               {
                  if (CommonValidation.Tag.forName(reader.getLocalName()) == CommonValidation.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonValidation.Tag.forName(reader.getLocalName()))
               {
                  case BACKGROUNDVALIDATIONMINUTES : {
                     backgroundValidationMinutes = elementAsLong(reader);
                     break;
                  }
                  case BACKGROUNDVALIDATION : {
                     backgroundValidation = elementAsBoolean(reader);
                     break;
                  }
                  case USEFASTFAIL : {
                     useFastFail = elementAsBoolean(reader);
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

   private CommonTimeOut parseTimeOut(XMLStreamReader reader, boolean isXa) throws XMLStreamException,
      ParserException, ValidateException
   {
      Long blockingTimeoutMillis = null;
      Long allocationRetryWaitMillis = null;
      Long idleTimeoutMinutes = null;
      Integer allocationRetry = null;
      Integer xaResourceTimeout = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (CommonConnDef.Tag.forName(reader.getLocalName()) == CommonConnDef.Tag.TIMEOUT)
               {

                  return new CommonTimeOutImpl(blockingTimeoutMillis, idleTimeoutMinutes, allocationRetry,
                                               allocationRetryWaitMillis, xaResourceTimeout);
               }
               else
               {
                  if (CommonTimeOut.Tag.forName(reader.getLocalName()) == CommonTimeOut.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonTimeOut.Tag.forName(reader.getLocalName()))
               {
                  case ALLOCATIONRETRYWAITMILLIS : {
                     allocationRetryWaitMillis = elementAsLong(reader);
                     break;
                  }
                  case ALLOCATIONRETRY : {
                     allocationRetry = elementAsInteger(reader);
                     break;
                  }
                  case BLOCKINGTIMEOUTMILLIS : {
                     blockingTimeoutMillis = elementAsLong(reader);
                     break;
                  }
                  case IDLETIMEOUTMINUTES : {
                     idleTimeoutMinutes = elementAsLong(reader);
                     break;
                  }
                  case XARESOURCETIMEOUT : {
                     if (!isXa)
                        throw new ParserException("Element:" + reader.getLocalName() +
                                                  "cannot be set without an xa-pool");
                     xaResourceTimeout = elementAsInteger(reader);
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
    * parse a single admin-oject tag
    *
    * @param reader the reader
    * @return the parsed {@link CommonAdminObject}
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    */
   protected CommonAdminObject parseAdminObjects(XMLStreamReader reader) throws XMLStreamException,
      ParserException
   {
      HashMap<String, String> configProperties = new HashMap<String, String>();

      //attributes reading
      boolean useJavaContext = true;
      String className = null;
      boolean enabled = true;
      String jndiName = null;
      String poolName = null;

      int attributeSize = reader.getAttributeCount();

      for (int i = 0; i < attributeSize; i++)
      {
         CommonAdminObject.Attribute attribute = CommonAdminObject.Attribute.forName(reader
            .getAttributeLocalName(i));
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case JNDINAME : {
               jndiName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case CLASS_NAME : {
               className = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            case USEJAVACONTEXT : {
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), true);
               break;
            }
            case POOL_NAME : {
               poolName = attributeAsString(reader, attribute.getLocalName());
               break;
            }
            default :
               throw new ParserException("Unexpected attribute:" + attribute.getLocalName() + "at " +
                                         reader.getLocalName());
         }
      }
      if (jndiName == null || jndiName.trim().equals(""))
         throw new ParserException("Missing jndiName mandatory attribute");

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (ResourceAdapter.Tag.forName(reader.getLocalName()) == ResourceAdapter.Tag.ADMIN_OBJECT)
               {

                  return new CommonAdminObjectImpl(configProperties, className, jndiName, poolName, enabled,
                                                   useJavaContext);
               }
               else
               {
                  if (CommonAdminObject.Tag.forName(reader.getLocalName()) == CommonAdminObject.Tag.UNKNOWN)
                  {
                     throw new ParserException("unexpected end tag" + reader.getLocalName());
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (CommonAdminObject.Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperties.put(attributeAsString(reader, "name"), elementAsString(reader));
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
