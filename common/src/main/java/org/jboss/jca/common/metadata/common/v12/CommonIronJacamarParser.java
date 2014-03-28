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
package org.jboss.jca.common.metadata.common.v12;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.Defaults;
import org.jboss.jca.common.api.metadata.common.CommonPool;
import org.jboss.jca.common.api.metadata.common.CommonSecurity;
import org.jboss.jca.common.api.metadata.common.CommonTimeOut;
import org.jboss.jca.common.api.metadata.common.CommonValidation;
import org.jboss.jca.common.api.metadata.common.Recovery;
import org.jboss.jca.common.api.metadata.common.v12.CommonConnDef;
import org.jboss.jca.common.api.metadata.resourceadapter.v12.ResourceAdapter;
import org.jboss.jca.common.api.validator.ValidateException;
import org.jboss.jca.common.metadata.ParserException;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 * A CommonIronJacamarParser.
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public abstract class CommonIronJacamarParser extends org.jboss.jca.common.metadata.common.v11.CommonIronJacamarParser
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   /**
    *
    * parse a single connection-definition tag
    *
    * @param reader the reader
    * @param isXA Is XA capable
    * @return the parse {@link CommonConnDef} object
    * @throws XMLStreamException XMLStreamException
    * @throws ParserException ParserException
    * @throws ValidateException ValidateException
    */
   protected CommonConnDef parseConnectionDefinitions(XMLStreamReader reader, Boolean isXA) throws XMLStreamException,
      ParserException, ValidateException
   {
      Map<String, String> configProperties = new HashMap<String, String>();
      CommonSecurity security = null;
      CommonTimeOut timeOut = null;
      CommonValidation validation = null;
      CommonPool pool = null;
      Recovery recovery = null;

      //attributes reading
      Boolean useJavaContext = Defaults.USE_JAVA_CONTEXT;
      String className = null;
      Boolean enabled = Defaults.ENABLED;
      String jndiName = null;
      String poolName = null;
      Boolean useCcm = Defaults.USE_CCM;
      Boolean sharable = Defaults.SHARABLE;
      Boolean enlistment = Defaults.ENLISTMENT;
      Boolean connectable = Defaults.CONNECTABLE;
      Boolean tracking = Defaults.TRACKING;
      int attributeSize = reader.getAttributeCount();

      if (isXA == null)
         isXA = Boolean.FALSE;

      for (int i = 0; i < attributeSize; i++)
      {
         CommonConnDef.Attribute attribute = CommonConnDef.Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute)
         {
            case ENABLED : {
               enabled = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.ENABLED);
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
               useJavaContext = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.USE_JAVA_CONTEXT);
               break;
            }
            case USE_CCM : {
               useCcm = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.USE_CCM);
               break;
            }
            case SHARABLE : {
               sharable = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.SHARABLE);
               break;
            }
            case ENLISTMENT : {
               enlistment = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.ENLISTMENT);
               break;
            }
            case CONNECTABLE : {
               connectable = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.CONNECTABLE);
               break;
            }
            case TRACKING : {
               tracking = attributeAsBoolean(reader, attribute.getLocalName(), Defaults.TRACKING);
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
                                               useJavaContext, useCcm, sharable, enlistment, connectable, tracking,
                                               pool, timeOut, validation,
                                               security, recovery, isXA);
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
                     timeOut = parseTimeOut(reader, isXA);
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
                     isXA = Boolean.TRUE;
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
}
