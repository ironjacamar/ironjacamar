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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.CommonBundle;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.spec.Activationspec;
import org.ironjacamar.common.api.metadata.spec.AdminObject;
import org.ironjacamar.common.api.metadata.spec.AuthenticationMechanism;
import org.ironjacamar.common.api.metadata.spec.ConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.api.metadata.spec.Connector.Version;
import org.ironjacamar.common.api.metadata.spec.CredentialInterfaceEnum;
import org.ironjacamar.common.api.metadata.spec.Icon;
import org.ironjacamar.common.api.metadata.spec.InboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.LicenseType;
import org.ironjacamar.common.api.metadata.spec.LocalizedXsdString;
import org.ironjacamar.common.api.metadata.spec.MessageListener;
import org.ironjacamar.common.api.metadata.spec.Messageadapter;
import org.ironjacamar.common.api.metadata.spec.OutboundResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.RequiredConfigProperty;
import org.ironjacamar.common.api.metadata.spec.ResourceAdapter;
import org.ironjacamar.common.api.metadata.spec.SecurityPermission;
import org.ironjacamar.common.api.metadata.spec.XsdString;
import org.ironjacamar.common.metadata.MetadataParser;
import org.ironjacamar.common.metadata.ParserException;
import org.ironjacamar.common.metadata.common.AbstractParser;

import static org.ironjacamar.common.api.metadata.spec.XsdString.NULL_XSDSTRING;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import org.jboss.logging.Messages;

/**
 * A RaParser.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
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
            //found a non tag..go on normally non-tag found at beginning are comments or DTD declaration
            iterate = reader.nextTag();
         }
         switch (iterate)
         {
            case END_ELEMENT : {
               // should mean we're done, so ignore it.
               break;
            }
            case START_ELEMENT : {
               if ("1.7".equals(reader.getAttributeValue(null, XML.ATTRIBUTE_VERSION)))
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONNECTOR : {
                        connector = parseConnector17(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }

               }
               else if ("1.6".equals(reader.getAttributeValue(null, XML.ATTRIBUTE_VERSION)))
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONNECTOR : {
                        connector = parseConnector16(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }

               }
               else if ("1.5".equals(reader.getAttributeValue(null, XML.ATTRIBUTE_VERSION)))
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONNECTOR : {
                        connector = parseConnector15(reader);
                        break;
                     }
                     default :
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                  }
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONNECTOR : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
               if (XML.ELEMENT_CONNECTOR.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_VENDOR_NAME :
                     case XML.ELEMENT_EIS_TYPE :
                     case XML.ELEMENT_LICENSE :
                     case XML.ELEMENT_VERSION :
                     case XML.ELEMENT_RESOURCEADAPTER :
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_DISPLAY_NAME :
                     case XML.ELEMENT_ICON :
                     case XML.ELEMENT_SPEC_VERSION :
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
                  case XML.ELEMENT_VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case XML.ELEMENT_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case XML.ELEMENT_RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter10(reader);
                     break;
                  }
                  case XML.ELEMENT_DESCRIPTION : {
                     if (description.size() > 0)
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                     else
                        description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_DISPLAY_NAME : {
                     if (displayName.size() > 0)
                        throw new ParserException(bundle.unexpectedElement(reader.getLocalName()));
                     else
                        displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_ICON : {
                     icon.add(parseIcon(reader));
                     break;
                  }
                  case XML.ELEMENT_SPEC_VERSION : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
               if (XML.ELEMENT_CONNECTOR.equals(reader.getLocalName()))
               {

                  //building and returning object
                  return new ConnectorImpl(Version.V_15, null, vendorName, eisType, resourceadapterVersion, license, 
                                           resourceadapter, null, true, description, displayName, icon, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_VENDOR_NAME :
                     case XML.ELEMENT_EIS_TYPE :
                     case XML.ELEMENT_LICENSE :
                     case XML.ELEMENT_RESOURCEADAPTER_VERSION :
                     case XML.ELEMENT_RESOURCEADAPTER :
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_DISPLAY_NAME :
                     case XML.ELEMENT_ICON :
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
                  case XML.ELEMENT_VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case XML.ELEMENT_RESOURCEADAPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case XML.ELEMENT_RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case XML.ELEMENT_DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_ICON : {
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
         XML.ATTRIBUTE_METADATA_COMPLETE));
      LicenseType license = null;
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
               if (XML.ELEMENT_CONNECTOR.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MODULE_NAME :
                     case XML.ELEMENT_VENDOR_NAME :
                     case XML.ELEMENT_EIS_TYPE :
                     case XML.ELEMENT_LICENSE :
                     case XML.ELEMENT_RESOURCEADAPTER_VERSION :
                     case XML.ELEMENT_RESOURCEADAPTER :
                     case XML.ELEMENT_REQUIRED_WORK_CONTEXT :
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_DISPLAY_NAME :
                     case XML.ELEMENT_ICON :
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
                  case XML.ELEMENT_MODULE_NAME : {
                     moduleName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case XML.ELEMENT_RESOURCEADAPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case XML.ELEMENT_RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case XML.ELEMENT_REQUIRED_WORK_CONTEXT : {
                     requiredWorkContext.add(elementAsXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_ICON : {
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
         XML.ATTRIBUTE_METADATA_COMPLETE));
      LicenseType license = null;
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
               if (XML.ELEMENT_CONNECTOR.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MODULE_NAME :
                     case XML.ELEMENT_VENDOR_NAME :
                     case XML.ELEMENT_EIS_TYPE :
                     case XML.ELEMENT_LICENSE :
                     case XML.ELEMENT_RESOURCEADAPTER_VERSION :
                     case XML.ELEMENT_RESOURCEADAPTER :
                     case XML.ELEMENT_REQUIRED_WORK_CONTEXT :
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_DISPLAY_NAME :
                     case XML.ELEMENT_ICON :
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
                  case XML.ELEMENT_MODULE_NAME : {
                     moduleName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_VENDOR_NAME : {
                     vendorName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_EIS_TYPE : {
                     eisType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_LICENSE : {
                     license = parseLicense(reader);
                     break;
                  }
                  case XML.ELEMENT_RESOURCEADAPTER_VERSION : {
                     resourceadapterVersion = elementAsXsdString(reader);
                  }
                     break;
                  case XML.ELEMENT_RESOURCEADAPTER : {
                     resourceadapter = parseResourceAdapter(reader);
                     break;
                  }
                  case XML.ELEMENT_REQUIRED_WORK_CONTEXT : {
                     requiredWorkContext.add(elementAsXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_DISPLAY_NAME : {
                     displayName.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_ICON : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      String lang = reader.getAttributeValue(null, XML.ATTRIBUTE_LANG);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_ICON.equals(reader.getLocalName()))
               {
                  //building and returning object
                  return new IconImpl(smallIcon, largeIcon, lang, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_SMALL_ICON :
                     case XML.ELEMENT_LARGE_ICON :
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
                  case XML.ELEMENT_SMALL_ICON : {
                     smallIcon = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_LARGE_ICON : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_RESOURCEADAPTER.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_RESOURCEADAPTER_CLASS :
                     case XML.ELEMENT_CONFIG_PROPERTY :
                     case XML.ELEMENT_OUTBOUND_RESOURCEADAPTER :
                     case XML.ELEMENT_INBOUND_RESOURCEADAPTER :
                     case XML.ELEMENT_ADMINOBJECT :
                     case XML.ELEMENT_SECURITY_PERMISSION :
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
                  case XML.ELEMENT_RESOURCEADAPTER_CLASS : {
                     resourceadapterClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }
                  case XML.ELEMENT_OUTBOUND_RESOURCEADAPTER : {
                     outboundResourceadapter = parseOutboundResourceadapter(reader);
                     break;
                  }
                  case XML.ELEMENT_INBOUND_RESOURCEADAPTER : {
                     inboundResourceadapter = parseInboundResourceadapter(reader);
                     break;
                  }
                  case XML.ELEMENT_ADMINOBJECT : {
                     adminobject.add(parseAdminObject(reader));
                     break;
                  }
                  case XML.ELEMENT_SECURITY_PERMISSION : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      String rsId = null;
      String tsId = null;
      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_RESOURCEADAPTER.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONFIG_PROPERTY :
                     case XML.ELEMENT_AUTHENTICATION_MECHANISM :
                     case XML.ELEMENT_MANAGEDCONNECTIONFACTORY_CLASS :
                     case XML.ELEMENT_CONNECTION_INTERFACE :
                     case XML.ELEMENT_CONNECTION_IMPL_CLASS :
                     case XML.ELEMENT_CONNECTIONFACTORY_INTERFACE :
                     case XML.ELEMENT_CONNECTIONFACTORY_IMPL_CLASS :
                     case XML.ELEMENT_REAUTHENTICATION_SUPPORT :
                     case XML.ELEMENT_SECURITY_PERMISSION :
                     case XML.ELEMENT_TRANSACTION_SUPPORT :
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
                  case XML.ELEMENT_CONFIG_PROPERTY : {
                     configProperties.add(parseConfigProperty(reader));
                     break;
                  }
                  case XML.ELEMENT_AUTHENTICATION_MECHANISM : {
                     authenticationMechanism.add(parseAuthenticationMechanism(reader));
                     break;
                  }
                  case XML.ELEMENT_MANAGEDCONNECTIONFACTORY_CLASS : {
                     managedConnectionFactoryClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_INTERFACE : {
                     connectionInterface = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_IMPL_CLASS : {
                     connectionImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTIONFACTORY_INTERFACE : {
                     connectionFactoryInterface = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTIONFACTORY_IMPL_CLASS : {
                     connectionFactoryImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_REAUTHENTICATION_SUPPORT : {
                     rsId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
                     reauthenticationSupport = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_SECURITY_PERMISSION : {
                     securityPermission.add(parseSecurityPermission(reader));
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_SUPPORT : {
                     tsId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_INBOUND_RESOURCEADAPTER.equals(reader.getLocalName()))
               {
                  //building and returning object
                  return new InboundResourceAdapterImpl(messageadapter, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MESSAGEADAPTER :
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
                  case XML.ELEMENT_MESSAGEADAPTER : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_MESSAGEADAPTER.equals(reader.getLocalName()))
               {
                  //trimming collections
                  messagelistener.trimToSize();

                  //building and returning object
                  return new MessageAdapterImpl(messagelistener, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MESSAGELISTENER :
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
                  case XML.ELEMENT_MESSAGELISTENER : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_MESSAGELISTENER.equals(reader.getLocalName()))
               {
                  //building and returning object
                  return new MessageListenerImpl(messagelistenerType, activationspec, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_MESSAGELISTENER_TYPE :
                     case XML.ELEMENT_ACTIVATIONSPEC :
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
                  case XML.ELEMENT_MESSAGELISTENER_TYPE : {
                     messagelistenerType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_ACTIVATIONSPEC : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {

         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_ACTIVATIONSPEC.equals(reader.getLocalName()))
               {
                  //trimming collections
                  requiredConfigProperty.trimToSize();
                  configProperty.trimToSize();

                  //building and returning object
                  return new ActivationSpecImpl(activationspecClass, requiredConfigProperty, configProperty, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_ACTIVATIONSPEC_CLASS :
                     case XML.ELEMENT_REQUIRED_CONFIG_PROPERTY :
                     case XML.ELEMENT_CONFIG_PROPERTY :
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
                  case XML.ELEMENT_ACTIVATIONSPEC_CLASS : {
                     activationspecClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_REQUIRED_CONFIG_PROPERTY : {
                     requiredConfigProperty.add(parseRequiredConfigProperty(reader));
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_REQUIRED_CONFIG_PROPERTY.equals(reader.getLocalName()))
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new RequiredConfigPropertyImpl(description, configPropertyName, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_CONFIG_PROPERTY_NAME :
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
                  case XML.ELEMENT_DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_NAME : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      String tsId = null;
      String rsId = null;

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_OUTBOUND_RESOURCEADAPTER.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONNECTION_DEFINITION :
                     case XML.ELEMENT_AUTHENTICATION_MECHANISM :
                     case XML.ELEMENT_TRANSACTION_SUPPORT :
                     case XML.ELEMENT_REAUTHENTICATION_SUPPORT :
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
                  case XML.ELEMENT_CONNECTION_DEFINITION : {
                     connectionDefinition.add(parseConncetionDefinition(reader));
                     break;
                  }
                  case XML.ELEMENT_AUTHENTICATION_MECHANISM : {
                     authenticationMechanism.add(parseAuthenticationMechanism(reader));
                     break;
                  }
                  case XML.ELEMENT_TRANSACTION_SUPPORT : {
                     tsId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
                     transactionSupport = TransactionSupportEnum.valueOf(reader.getElementText().trim());
                     break;
                  }
                  case XML.ELEMENT_REAUTHENTICATION_SUPPORT : {
                     rsId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_CONNECTION_DEFINITION.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_CONFIG_PROPERTY :
                     case XML.ELEMENT_MANAGEDCONNECTIONFACTORY_CLASS :
                     case XML.ELEMENT_CONNECTIONFACTORY_INTERFACE :
                     case XML.ELEMENT_CONNECTIONFACTORY_IMPL_CLASS :
                     case XML.ELEMENT_CONNECTION_INTERFACE :
                     case XML.ELEMENT_CONNECTION_IMPL_CLASS :
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
                  case XML.ELEMENT_CONFIG_PROPERTY : {
                     configProperty.add(parseConfigProperty(reader));
                     break;
                  }
                  case XML.ELEMENT_MANAGEDCONNECTIONFACTORY_CLASS : {
                     managedconnectionfactoryClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTIONFACTORY_INTERFACE : {
                     connectionfactoryInterface = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTIONFACTORY_IMPL_CLASS : {
                     connectionfactoryImplClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_INTERFACE : {
                     connectionInterface = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONNECTION_IMPL_CLASS : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_AUTHENTICATION_MECHANISM.equals(reader.getLocalName()))
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new AuthenticationMechanismImpl(description, authenticationMechanismType, credentialInterface,
                        id, cIId);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_AUTHENTICATION_MECHANISM_TYPE :
                     case XML.ELEMENT_CREDENTIAL_INTERFACE :
                     case XML.ELEMENT_DESCRIPTION :
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
                  case XML.ELEMENT_AUTHENTICATION_MECHANISM_TYPE : {
                     authenticationMechanismType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CREDENTIAL_INTERFACE : {
                     cIId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
                     credentialInterface = CredentialInterfaceEnum.forName(reader.getElementText().trim());
                     break;
                  }
                  case XML.ELEMENT_DESCRIPTION : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_ADMINOBJECT.equals(reader.getLocalName()))
               {
                  //trimming collections
                  configProperty.trimToSize();

                  //building and returning object
                  return new AdminObjectImpl(adminobjectInterface, adminobjectClass, configProperty, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_ADMINOBJECT_CLASS :
                     case XML.ELEMENT_ADMINOBJECT_INTERFACE :
                     case XML.ELEMENT_CONFIG_PROPERTY :
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
                  case XML.ELEMENT_ADMINOBJECT_CLASS : {
                     adminobjectClass = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_ADMINOBJECT_INTERFACE : {
                     adminobjectInterface = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_CONFIG_PROPERTY.equals(reader.getLocalName()))
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
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_CONFIG_PROPERTY_NAME :
                     case XML.ELEMENT_CONFIG_PROPERTY_TYPE :
                     case XML.ELEMENT_CONFIG_PROPERTY_VALUE :
                     case XML.ELEMENT_CONFIG_PROPERTY_IGNORE :
                     case XML.ELEMENT_CONFIG_PROPERTY_CONFIDENTIAL :
                     case XML.ELEMENT_CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE :
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
                  case XML.ELEMENT_DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_NAME : {
                     configPropertyName = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_TYPE : {
                     configPropertyType = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_VALUE : {
                     configPropertyValue = elementAsXsdString(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_IGNORE : {
                     ignoreId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
                     configPropertyIgnore = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_CONFIDENTIAL : {
                     confidId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
                     configPropertyConfidential = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE : {
                     updatesId = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_SECURITY_PERMISSION.equals(reader.getLocalName()))
               {
                  //trimming collections
                  description.trimToSize();

                  //building and returning object
                  return new SecurityPermissionImpl(description, securityPermissionSpec, id);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_DESCRIPTION :
                     case XML.ELEMENT_SECURITY_PERMISSION_SPEC :
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
                  case XML.ELEMENT_DESCRIPTION : {
                     description.add(elementAsLocalizedXsdString(reader));
                     break;
                  }
                  case XML.ELEMENT_SECURITY_PERMISSION_SPEC : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      boolean licenseRequired = false;
      String lrid = null;
      ArrayList<LocalizedXsdString> description = new ArrayList<LocalizedXsdString>();

      while (reader.hasNext())
      {
         switch (reader.nextTag())
         {
            case END_ELEMENT : {
               if (XML.ELEMENT_LICENSE.equals(reader.getLocalName()))
               {
                  description.trimToSize();
                  return new LicenseTypeImpl(description, licenseRequired, id, lrid);
               }
               else
               {
                  switch (reader.getLocalName())
                  {
                     case XML.ELEMENT_LICENSE_REQUIRED :
                     case XML.ELEMENT_DESCRIPTION :
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
                  case XML.ELEMENT_LICENSE_REQUIRED : {
                     lrid = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
                     licenseRequired = elementAsBoolean(reader);
                     break;
                  }
                  case XML.ELEMENT_DESCRIPTION : {
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
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      return new XsdString(reader.getElementText().trim(), id, reader.getLocalName());
   }

   private LocalizedXsdString elementAsLocalizedXsdString(XMLStreamReader reader) throws XMLStreamException
   {
      String id = reader.getAttributeValue(null, XML.ATTRIBUTE_ID);
      String lang = reader.getAttributeValue(null, XML.ATTRIBUTE_LANG);
      return new LocalizedXsdString(reader.getElementText().trim(), id, lang, reader.getLocalName());
   }
}
