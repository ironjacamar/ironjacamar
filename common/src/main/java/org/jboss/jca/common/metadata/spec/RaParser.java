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
package org.jboss.jca.common.metadata.spec;

import org.jboss.jca.common.CommonBundle;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.spec.Activationspec;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.spec.ConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.spec.Icon;
import org.jboss.jca.common.api.metadata.spec.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.LicenseType;
import org.jboss.jca.common.api.metadata.spec.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.spec.MessageListener;
import org.jboss.jca.common.api.metadata.spec.Messageadapter;
import org.jboss.jca.common.api.metadata.spec.OutboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.RequiredConfigProperty;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.SecurityPermission;
import org.jboss.jca.common.api.metadata.spec.XsdString;
import org.jboss.jca.common.metadata.MetadataParser;
import org.jboss.jca.common.metadata.ParserException;
import org.jboss.jca.common.metadata.common.AbstractParser;

import static org.jboss.jca.common.api.metadata.spec.XsdString.NULL_XSDSTRING;

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
 * A RaParser.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 *
 */
public class RaParser extends AbstractParser implements MetadataParser<Connector>
{
   /** The bundle */
   private static CommonBundle bundle = Messages.getBundle(CommonBundle.class);

   @Override
   public Connector parse(InputStream xmlInputStream) throws Exception
   {
      XMLStreamReader reader = null;

      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
      reader = inputFactory.createXMLStreamReader(xmlInputStream);
      return parse(reader);
   }

   @Override
   public Connector parse(XMLStreamReader reader) throws Exception
   {
      Connector connector = null;

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
               if ("1.7".equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector17(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }

               }
               else if ("1.6".equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector16(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }

               }
               else if ("1.5".equals(reader.getAttributeValue(null, "version")))
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector15(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }
               }
               else
               {
                  switch (Tag.forName(reader.getLocalName()))
                  {
                     case CONNECTOR : {
                        connector = parseConnector10(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }
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
      return connector;

   }

   private Connector parseConnector10(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      LicenseType license = null;
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());;
      ArrayList<Icon> icon = new ArrayList<Icon>();
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString eisType = NULL_XSDSTRING;
      ResourceAdapter resourceadapter = null;
      XsdString vendorName = NULL_XSDSTRING;
      ArrayList<LocalizedXsdString> displayName = new ArrayList<LocalizedXsdString>();
      XsdString resourceadapterVersion = NULL_XSDSTRING;
      XsdString specVersion = NULL_XSDSTRING;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  //trimming collections
                  icon.trimToSize();
                  description.trimToSize();
                  //erase lang attribute 
                  for (Icon i : icon)
                     ((IconImpl)i).setLang(null);
                  for (LocalizedXsdString d : description)
                     d.setLang(null);
                  for (LocalizedXsdString n : displayName)
                     n.setLang(null);

                  //building and returning object
                  return new ConnectorImpl(Version.V_10, null, vendorName, eisType, resourceadapterVersion,
                                           license, resourceadapter, null, true,
                                           description, displayName, icon, id);
               }
               else
               {
                  if (XML.Connector10Tag.forName(reader.getLocalName()) == XML.Connector10Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.Connector10Tag.forName(reader.getLocalName()))
               {
                  case VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter10(reader);
                     break;
                  }
                  case DESCRIPTION : {
                     if (description.size() > 0)
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                     else
                        description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case DISPLAY_NAME : {
                     if (displayName.size() > 0)
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                     else
                        displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case ICON : {
                     icon.add(parseIcon(reader));
                     break;
                  }
                  case SPEC_VERSION : {
                     specVersion = elementAsXsdString(reader);
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

   private Connector parseConnector15(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      LicenseType license = null;
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());;
      XsdString eisType = NULL_XSDSTRING;
      ResourceAdapter resourceadapter = null;
      XsdString vendorName = NULL_XSDSTRING;
      XsdString resourceadapterVersion = NULL_XSDSTRING;
      ArrayList<Icon> icon = new ArrayList<Icon>();
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      ArrayList<LocalizedXsdString> displayName = new ArrayList<LocalizedXsdString>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {

                  //building and returning object
                  return new ConnectorImpl(Version.V_15, null, vendorName, eisType, resourceadapterVersion, license, 
                                           resourceadapter, null, true, description, displayName, icon, id);
               }
               else
               {
                  if (XML.Connector15Tag.forName(reader.getLocalName()) == XML.Connector15Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.Connector15Tag.forName(reader.getLocalName()))
               {
                  case VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case RESOURCEADAPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case ICON : {
                     icon.add(parseIcon(reader));
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

   private Connector parseConnector16(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      boolean metadataComplete = Boolean.valueOf(reader.getAttributeValue(null,
            XML.Connector16Attribute.METADATA_COMPLETE.getLocalName()));;
      LicenseType license = null;
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());;
      ArrayList<Icon> icon = new ArrayList<Icon>();
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      ArrayList<LocalizedXsdString> displayName = new ArrayList<LocalizedXsdString>();
      XsdString eisType = NULL_XSDSTRING;
      ResourceAdapter resourceadapter = null;
      XsdString vendorName = NULL_XSDSTRING;
      XsdString moduleName = null;
      ArrayList<XsdString> requiredWorkContext = new ArrayList<XsdString>();
      XsdString resourceadapterVersion = NULL_XSDSTRING;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  //trimming collections
                  icon.trimToSize();
                  description.trimToSize();
                  displayName.trimToSize();
                  requiredWorkContext.trimToSize();
                  //building and returning object
                  return new ConnectorImpl(Version.V_16, moduleName, vendorName, eisType, resourceadapterVersion,
                                           license, resourceadapter, requiredWorkContext, metadataComplete,
                                           description, displayName, icon, id);
               }
               else
               {
                  if (XML.Connector16Tag.forName(reader.getLocalName()) == XML.Connector16Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {

               switch (XML.Connector16Tag.forName(reader.getLocalName()))
               {
                  case MODULE_NAME : {
                     moduleName = elementAsXsdString(reader);
                     break;
                  }
                  case VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case RESOURCEADAPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case REQUIRED_WORK_CONTEXT : {
                     requiredWorkContext.add(elementAsXsdString(reader));
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case ICON : {
                     icon.add(parseIcon(reader));
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

   private Connector parseConnector17(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      boolean metadataComplete = Boolean.valueOf(reader.getAttributeValue(null,
         XML.Connector17Attribute.METADATA_COMPLETE.getLocalName()));;
      LicenseType license = null;
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());;
      ArrayList<Icon> icon = new ArrayList<Icon>();
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      ArrayList<LocalizedXsdString> displayName = new ArrayList<LocalizedXsdString>();
      XsdString eisType = NULL_XSDSTRING;
      ResourceAdapter resourceadapter = null;
      XsdString vendorName = NULL_XSDSTRING;
      XsdString moduleName = null;
      ArrayList<XsdString> requiredWorkContext = new ArrayList<XsdString>();
      XsdString resourceadapterVersion = NULL_XSDSTRING;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (Tag.forName(reader.getLocalName()) == Tag.CONNECTOR)
               {
                  //trimming collections
                  icon.trimToSize();
                  description.trimToSize();
                  displayName.trimToSize();
                  requiredWorkContext.trimToSize();
                  //building and returning object
                  return new ConnectorImpl(Version.V_17, moduleName, vendorName, eisType, resourceadapterVersion,
                                           license, resourceadapter, requiredWorkContext, metadataComplete,
                                           description, displayName, icon, id);
               }
               else
               {
                  if (XML.Connector17Tag.forName(reader.getLocalName()) == XML.Connector17Tag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {

               switch (XML.Connector17Tag.forName(reader.getLocalName()))
               {
                  case MODULE_NAME : {
                     moduleName = elementAsXsdString(reader);
                     break;
                  }
                  case VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case RESOURCEADAPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case REQUIRED_WORK_CONTEXT : {
                     requiredWorkContext.add(elementAsXsdString(reader));
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case ICON : {
                     icon.add(parseIcon(reader));
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

   private Icon parseIcon(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      XsdString largeIcon = null;
      XsdString smallIcon = null;

      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());
      String lang = reader.getAttributeValue(null, XML.IconAttribute.LANG.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.Connector16Tag.forName(reader.getLocalName()) == XML.Connector16Tag.ICON)
               {
                  //building and returning object

                  return new IconImpl(smallIcon, largeIcon, lang, id);

               }
               else
               {
                  if (XML.IconTag.forName(reader.getLocalName()) == XML.IconTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.IconTag.forName(reader.getLocalName()))
               {
                  case SMALL_ICON : {
                     smallIcon = elementAsXsdString(reader);
                     break;
                  }
                  case LARGE_ICON : {
                     largeIcon = elementAsXsdString(reader);
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

   private ResourceAdapter parseResourceAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      XsdString resourceadapterClass = null;
      OutboundResourceAdapter outboundResourceadapter = null;
      ArrayList<SecurityPermission> securityPermission = new ArrayList<SecurityPermission>();
      InboundResourceAdapter inboundResourceadapter = null;
      ArrayList<AdminObject> adminobject = new ArrayList<AdminObject>();
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.Connector16Tag.forName(reader.getLocalName()) == XML.Connector16Tag.RESOURCEADAPTER)
               {
                  //trimming collections
                  configProperty.trimToSize();
                  securityPermission.trimToSize();
                  adminobject.trimToSize();

                  //building and returning object
                  return new ResourceAdapterImpl(resourceadapterClass, configProperty, outboundResourceadapter,
                        inboundResourceadapter, adminobject, securityPermission, id);

               }
               else
               {
                  if (XML.ResourceAdapterTag.forName(reader.getLocalName()) == XML.ResourceAdapterTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {

               switch (XML.ResourceAdapterTag.forName(reader.getLocalName()))
               {
                  case RESOURCEADAPTER_CLASS : {
                     resourceadapterClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }

                  case OUTBOUND_RESOURCEADAPTER : {
                     outboundResourceadapter = parseOutboundResourceadapter(reader);
                     break;
                  }
                  case INBOUND_RESOURCEADAPTER : {
                     inboundResourceadapter = parseInboundResourceadapter(reader);
                     break;
                  }
                  case ADMINOBJECT : {
                     adminobject.add(parseAdminObject(reader));
                     break;
                  }
                  case SECURITY_PERMISSION : {
                     securityPermission.add(parseSecurityPermission(reader));
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

   private ResourceAdapter parseResourceAdapter10(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      XsdString managedConnectionFactoryClass = NULL_XSDSTRING;
      XsdString connectionFactoryInterface = NULL_XSDSTRING;
      XsdString connectionFactoryImplClass = NULL_XSDSTRING;
      XsdString connectionInterface = NULL_XSDSTRING;
      XsdString connectionImplClass = NULL_XSDSTRING;
      TransactionSupportEnum transactionSupport = null;
      ArrayList<AuthenticationMechanism> authenticationMechanism = new ArrayList<AuthenticationMechanism>();
      ArrayList<ConfigProperty> configProperties = new ArrayList<ConfigProperty>();
      boolean reauthenticationSupport = false;
      ArrayList<SecurityPermission> securityPermission = new ArrayList<SecurityPermission>();
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());
      String rsId = null;
      String tsId = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.Connector10Tag.forName(reader.getLocalName()) == XML.Connector10Tag.RESOURCEADAPTER)
               {
                  //trimming collections
                  authenticationMechanism.trimToSize();
                  configProperties.trimToSize();
                  securityPermission.trimToSize();

                  List<ConnectionDefinition> cds = new ArrayList<ConnectionDefinition>(1);
                  ConnectionDefinition cd = new ConnectionDefinitionImpl(managedConnectionFactoryClass,
                                                                         configProperties,
                                                                         connectionFactoryInterface,
                                                                         connectionFactoryImplClass,
                                                                         connectionInterface, 
                                                                         connectionImplClass, id);
                  cds.add(cd);

                  OutboundResourceAdapter ora = new OutboundResourceAdapterImpl(cds,
                                                                                transactionSupport,
                                                                                authenticationMechanism,
                                                                                reauthenticationSupport,
                                                                                id, tsId, rsId);

                  //building and returning object
                  return new ResourceAdapterImpl(null, null, ora, null, null, securityPermission, id);
               }
               else
               {
                  if (XML.ResourceAdapterTag.forName(reader.getLocalName()) == XML.ResourceAdapterTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {

               switch (XML.ResourceAdapter10Tag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperties.add(parseConfigProperty(reader));
                     break;
                  }
                  case AUTHENTICATION_MECHANISM : {
                     authenticationMechanism.add(parseAuthenticationMechanism(reader));
                     break;
                  }
                  case MANAGEDCONNECTIONFACTORY_CLASS : {
                     managedConnectionFactoryClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_INTERFACE : {
                     connectionInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_IMPL_CLASS : {
                     connectionImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_INTERFACE : {
                     connectionFactoryInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_IMPL_CLASS : {
                     connectionFactoryImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case REAUTHENTICATION_SUPPORT : {
                     rsId = reader.getAttributeValue(null, "id");
                     reauthenticationSupport = elementAsBoolean(reader);
                     break;
                  }
                  case SECURITY_PERMISSION : {
                     securityPermission.add(parseSecurityPermission(reader));
                     break;
                  }
                  case TRANSACTION_SUPPORT : {
                     tsId = reader.getAttributeValue(null, "id");
                     transactionSupport = TransactionSupportEnum.valueOf(reader.getElementText().trim());
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

   private InboundResourceAdapter parseInboundResourceadapter(XMLStreamReader reader) throws XMLStreamException,
         ParserException
   {
      Messageadapter messageadapter = null;
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ResourceAdapterTag.forName(reader.getLocalName()) ==
                   XML.ResourceAdapterTag.INBOUND_RESOURCEADAPTER)
               {

                  //building and returning object
                  return new InboundResourceAdapterImpl(messageadapter, id);

               }
               else
               {
                  if (XML.InboundResourceAdapterTag.forName(reader.getLocalName()) ==
                      XML.InboundResourceAdapterTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.InboundResourceAdapterTag.forName(reader.getLocalName()))
               {
                  case MESSAGEADAPTER : {
                     messageadapter = parseMessageAdapter(reader);
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

   private Messageadapter parseMessageAdapter(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<MessageListener> messagelistener = new ArrayList<MessageListener>();
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.InboundResourceAdapterTag.forName(reader.getLocalName()) == 
                   XML.InboundResourceAdapterTag.MESSAGEADAPTER)
               {
                  //trimming collections
                  messagelistener.trimToSize();

                  //building and returning object
                  return new MessageAdapterImpl(messagelistener, id);

               }
               else
               {
                  if (XML.MessageAdapterTag.forName(reader.getLocalName()) == XML.MessageAdapterTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.MessageAdapterTag.forName(reader.getLocalName()))
               {
                  case MESSAGELISTENER : {
                     messagelistener.add(parseMessageListener(reader));
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

   private MessageListener parseMessageListener(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      Activationspec activationspec = null;
      XsdString messagelistenerType = NULL_XSDSTRING;
      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.MessageAdapterTag.forName(reader.getLocalName()) == XML.MessageAdapterTag.MESSAGELISTENER)
               {
                  //building and returning object
                  return new MessageListenerImpl(messagelistenerType, activationspec, id);
               }
               else
               {
                  if (XML.MessageListenerTag.forName(reader.getLocalName()) == XML.MessageListenerTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.MessageListenerTag.forName(reader.getLocalName()))
               {
                  case MESSAGELISTENER_TYPE : {
                     messagelistenerType = elementAsXsdString(reader);
                     break;
                  }
                  case ACTIVATIONSPEC : {
                     activationspec = parseActivationspec(reader);
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

   private Activationspec parseActivationspec(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<RequiredConfigProperty> requiredConfigProperty = new ArrayList<RequiredConfigProperty>();
      XsdString activationspecClass = NULL_XSDSTRING;
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.MessageListenerTag.forName(reader.getLocalName()) == XML.MessageListenerTag.ACTIVATIONSPEC)
               {

                  //trimming collections
                  requiredConfigProperty.trimToSize();
                  configProperty.trimToSize();

                  //building and returning object
                  return new ActivationSpecImpl(activationspecClass, requiredConfigProperty, configProperty, id);
               }
               else
               {
                  if (XML.ActivationSpecTag.forName(reader.getLocalName()) == XML.ActivationSpecTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.ActivationSpecTag.forName(reader.getLocalName()))
               {
                  case ACTIVATIONSPEC_CLASS : {
                     activationspecClass = elementAsXsdString(reader);
                     break;
                  }
                  case REQUIRED_CONFIG_PROPERTY : {
                     requiredConfigProperty.add(parseRequiredConfigProperty(reader));
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
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

   private RequiredConfigProperty parseRequiredConfigProperty(XMLStreamReader reader) throws XMLStreamException,
         ParserException
   {
      XsdString configPropertyName = NULL_XSDSTRING;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ActivationSpecTag.forName(reader.getLocalName()) ==
                   XML.ActivationSpecTag.REQUIRED_CONFIG_PROPERTY)
               {

                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new RequiredConfigPropertyImpl(description, configPropertyName, id);
               }
               else
               {
                  if (XML.RequiredConfigPropertyTag.forName(reader.getLocalName()) ==
                      XML.RequiredConfigPropertyTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.RequiredConfigPropertyTag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case CONFIG_PROPERTY_NAME : {
                     configPropertyName = elementAsXsdString(reader);
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

   private OutboundResourceAdapter parseOutboundResourceadapter(XMLStreamReader reader) throws XMLStreamException,
         ParserException
   {
      boolean reauthenticationSupport = false;
      TransactionSupportEnum transactionSupport = null;
      ArrayList<ConnectionDefinition> connectionDefinition = new ArrayList<ConnectionDefinition>();
      ArrayList<AuthenticationMechanism> authenticationMechanism = new ArrayList<AuthenticationMechanism>();
      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());
      String tsId = null;
      String rsId = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ResourceAdapterTag.forName(reader.getLocalName()) == 
                   XML.ResourceAdapterTag.OUTBOUND_RESOURCEADAPTER)
               {

                  //trimming collections
                  authenticationMechanism.trimToSize();
                  connectionDefinition.trimToSize();

                  //building and returning object
                  return new OutboundResourceAdapterImpl(connectionDefinition, transactionSupport,
                        authenticationMechanism, reauthenticationSupport, id, tsId, rsId);

               }
               else
               {
                  if (XML.OutboundResourceAdapterTag.forName(reader.getLocalName()) ==
                      XML.OutboundResourceAdapterTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.OutboundResourceAdapterTag.forName(reader.getLocalName()))
               {
                  case CONNECTION_DEFINITION : {
                     connectionDefinition.add(parseConncetionDefinition(reader));
                     break;
                  }
                  case AUTHENTICATION_MECHANISM : {
                     authenticationMechanism.add(parseAuthenticationMechanism(reader));
                     break;
                  }
                  case TRANSACTION_SUPPORT : {
                     tsId = reader.getAttributeValue(null, "id");
                     transactionSupport = TransactionSupportEnum.valueOf(reader.getElementText().trim());
                     break;
                  }
                  case REAUTHENTICATION_SUPPORT : {
                     rsId = reader.getAttributeValue(null, "id");
                     reauthenticationSupport = elementAsBoolean(reader);
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

   private ConnectionDefinition parseConncetionDefinition(XMLStreamReader reader) throws XMLStreamException,
         ParserException
   {
      XsdString managedconnectionfactoryClass = NULL_XSDSTRING;
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      XsdString connectionImplClass = NULL_XSDSTRING;
      XsdString connectionInterface = NULL_XSDSTRING;
      XsdString connectionfactoryImplClass = NULL_XSDSTRING;
      XsdString connectionfactoryInterface = NULL_XSDSTRING;
      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.OutboundResourceAdapterTag.forName(reader.getLocalName()) == 
                   XML.OutboundResourceAdapterTag.CONNECTION_DEFINITION)
               {
                  //trimming collections
                  configProperty.trimToSize();

                  //building and returning object
                  return new ConnectionDefinitionImpl(managedconnectionfactoryClass, configProperty,
                        connectionfactoryInterface, connectionfactoryImplClass, connectionInterface,
                        connectionImplClass, id);

               }
               else
               {
                  if (XML.ConnectionDefinitionTag.forName(reader.getLocalName()) == XML.ConnectionDefinitionTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.ConnectionDefinitionTag.forName(reader.getLocalName()))
               {
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }
                  case MANAGEDCONNECTIONFACTORY_CLASS : {
                     managedconnectionfactoryClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_INTERFACE : {
                     connectionfactoryInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTIONFACTORY_IMPL_CLASS : {
                     connectionfactoryImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_INTERFACE : {
                     connectionInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONNECTION_IMPL_CLASS : {
                     connectionImplClass = elementAsXsdString(reader);
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

   private AuthenticationMechanism parseAuthenticationMechanism(XMLStreamReader reader) throws XMLStreamException,
         ParserException
   {
      XsdString authenticationMechanismType = NULL_XSDSTRING;
      CredentialInterfaceEnum credentialInterface = null;
      String cIId = null;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();

      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.OutboundResourceAdapterTag.forName(reader.getLocalName()) == 
                   XML.OutboundResourceAdapterTag.AUTHENTICATION_MECHANISM)
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new AuthenticationMechanismImpl(description, authenticationMechanismType, credentialInterface,
                        id, cIId);
               }
               else
               {
                  if (XML.AuthenticationMechanismTag.forName(reader.getLocalName()) ==
                      XML.AuthenticationMechanismTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.AuthenticationMechanismTag.forName(reader.getLocalName()))
               {
                  case AUTHENTICATION_MECHANISM_TYPE : {
                     authenticationMechanismType = elementAsXsdString(reader);
                     break;
                  }
                  case CREDENTIAL_INTERFACE : {
                     cIId = reader.getAttributeValue(null, "id");
                     credentialInterface = CredentialInterfaceEnum.forName(reader.getElementText().trim());
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
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

   private AdminObject parseAdminObject(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<ConfigProperty> configProperty = new ArrayList<ConfigProperty>();
      XsdString adminobjectInterface = NULL_XSDSTRING;
      XsdString adminobjectClass = NULL_XSDSTRING;

      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ResourceAdapterTag.forName(reader.getLocalName()) == XML.ResourceAdapterTag.ADMINOBJECT)
               {
                  //trimming collections
                  configProperty.trimToSize();

                  //building and returning object
                  return new AdminObjectImpl(adminobjectInterface, adminobjectClass, configProperty, id);

               }
               else
               {
                  if (XML.AdminObjectTag.forName(reader.getLocalName()) == XML.AdminObjectTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.AdminObjectTag.forName(reader.getLocalName()))
               {
                  case ADMINOBJECT_CLASS : {
                     adminobjectClass = elementAsXsdString(reader);
                     break;
                  }
                  case ADMINOBJECT_INTERFACE : {
                     adminobjectInterface = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
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

   private ConfigProperty parseConfigProperty(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString configPropertyType = NULL_XSDSTRING;
      XsdString configPropertyValue = NULL_XSDSTRING;
      Boolean configPropertyIgnore = null;
      XsdString configPropertyName = NULL_XSDSTRING;
      Boolean configPropertySupportsDynamicUpdates = null;
      Boolean configPropertyConfidential = null;
      String ignoreId = null;
      String updatesId = null;
      String confidId = null;

      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ResourceAdapterTag.forName(reader.getLocalName()) == XML.ResourceAdapterTag.CONFIG_PROPERTY)
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new ConfigPropertyImpl(description, configPropertyName, configPropertyType,
                                                configPropertyValue, configPropertyIgnore,
                                                configPropertySupportsDynamicUpdates,
                                                configPropertyConfidential, id, false, null,
                                                ignoreId, updatesId, confidId);
               }
               else
               {
                  if (XML.ConfigPropertyTag.forName(reader.getLocalName()) == XML.ConfigPropertyTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.ConfigPropertyTag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case CONFIG_PROPERTY_NAME : {
                     configPropertyName = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_TYPE : {
                     configPropertyType = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_VALUE : {
                     configPropertyValue = elementAsXsdString(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_IGNORE : {
                     ignoreId = reader.getAttributeValue(null, "id");
                     configPropertyIgnore = elementAsBoolean(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_CONFIDENTIAL : {
                     confidId = reader.getAttributeValue(null, "id");
                     configPropertyConfidential = elementAsBoolean(reader);
                     break;
                  }
                  case CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE : {
                     updatesId = reader.getAttributeValue(null, "id");
                     configPropertySupportsDynamicUpdates = elementAsBoolean(reader);
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

   private SecurityPermission parseSecurityPermission(XMLStreamReader reader) throws XMLStreamException,
         ParserException
   {
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();
      XsdString securityPermissionSpec = NULL_XSDSTRING;

      //getting attributes
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ResourceAdapterTag.forName(reader.getLocalName()) == 
                   XML.ResourceAdapterTag.SECURITY_PERMISSION)
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new SecurityPermissionImpl(description, securityPermissionSpec, id);

               }
               else
               {
                  if (XML.SecurityPermissionTag.forName(reader.getLocalName()) == XML.SecurityPermissionTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.SecurityPermissionTag.forName(reader.getLocalName()))
               {
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case SECURITY_PERMISSION_SPEC : {
                     securityPermissionSpec = elementAsXsdString(reader);
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

   private LicenseType parseLicense(XMLStreamReader reader) throws XMLStreamException, ParserException
   {
      String id = reader.getAttributeValue(null, XML.IdAttribute.ID.getLocalName());;
      boolean licenseRequired = false;
      String lrid = null;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.Connector16Tag.forName(reader.getLocalName()) == XML.Connector16Tag.LICENSE)
               {
                  description.trimToSize();
                  return new LicenseTypeImpl(description, licenseRequired, id, lrid);

               }
               else
               {
                  if (XML.LicenseTag.forName(reader.getLocalName()) == XML.LicenseTag.UNKNOWN)
                  {
                     throw new ParserException(bundle.unexpectedEndTag(reader.getLocalName()));
                  }
               }
               break;
            }
            case START_ELEMENT : {
               switch (XML.LicenseTag.forName(reader.getLocalName()))
               {
                  case LICENSE_REQUIRED : {
                     lrid = reader.getAttributeValue(null, "id");
                     licenseRequired = elementAsBoolean(reader);
                     break;
                  }
                  case DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
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

   private XsdString elementAsXsdString(XMLStreamReader reader) throws XMLStreamException
   {
      String id = reader.getAttributeValue(null, "id");
      return new XsdString(reader.getElementText().trim(), id, reader.getLocalName());
   }

   private LocalizedXsdString elementAsLocalizedXsdString(XMLStreamReader reader) throws XMLStreamException
   {
      String id = reader.getAttributeValue(null, "id");
      String lang = reader.getAttributeValue(null, "lang");
      return new LocalizedXsdString(reader.getElementText().trim(), id, lang, reader.getLocalName());
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

      /** connector tag name
       *
       */
      CONNECTOR("connector");

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
