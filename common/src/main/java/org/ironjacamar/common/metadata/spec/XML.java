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

/**
 * XML elements / attributes for ra.xml
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
interface XML
{
   /**
    * id
    */
   public static final String ATTRIBUTE_ID = "id";

   /**
    * lang
    */
   public static final String ATTRIBUTE_LANG = "lang";

   /**
    * metadata-complete
    */
   public static final String ATTRIBUTE_METADATA_COMPLETE = "metadata-complete";

   /**
    * version
    */
   public static final String ATTRIBUTE_VERSION = "version";

   /**
    * activationspec
    */
   public static final String ELEMENT_ACTIVATIONSPEC = "activationspec";

   /**
    * activationspec-class
    */
   public static final String ELEMENT_ACTIVATIONSPEC_CLASS = "activationspec-class";

   /**
    * adminobject
    */
   public static final String ELEMENT_ADMINOBJECT = "adminobject";

   /**
    * adminobject-class
    */
   public static final String ELEMENT_ADMINOBJECT_CLASS = "adminobject-class";

   /**
    * adminobject-interface
    */
   public static final String ELEMENT_ADMINOBJECT_INTERFACE = "adminobject-interface";

   /**
    * authentication-mechanism
    */
   public static final String ELEMENT_AUTHENTICATION_MECHANISM = "authentication-mechanism";

   /**
    * authentication-mechanism-type
    */
   public static final String ELEMENT_AUTHENTICATION_MECHANISM_TYPE = "authentication-mechanism-type";

   /**
    * config-property
    */
   public static final String ELEMENT_CONFIG_PROPERTY = "config-property";

   /**
    * config-property-confidential
    */
   public static final String ELEMENT_CONFIG_PROPERTY_CONFIDENTIAL = "config-property-confidential";

   /**
    * config-property-ignore
    */
   public static final String ELEMENT_CONFIG_PROPERTY_IGNORE = "config-property-ignore";

   /**
    * config-property-name
    */
   public static final String ELEMENT_CONFIG_PROPERTY_NAME = "config-property-name";

   /**
    * config-property-supports-dynamic-updates
    */
   public static final String ELEMENT_CONFIG_PROPERTY_SUPPORT_DYNAMIC_UPDATE =
      "config-property-supports-dynamic-updates";

   /**
    * config-property-type
    */
   public static final String ELEMENT_CONFIG_PROPERTY_TYPE = "config-property-type";

   /**
    * config-property-value
    */
   public static final String ELEMENT_CONFIG_PROPERTY_VALUE = "config-property-value";

   /**
    * connectionfactory-impl-class
    */
   public static final String ELEMENT_CONNECTIONFACTORY_IMPL_CLASS = "connectionfactory-impl-class";

   /**
    * connectionfactory-interface
    */
   public static final String ELEMENT_CONNECTIONFACTORY_INTERFACE = "connectionfactory-interface";

   /**
    * connection-definition
    */
   public static final String ELEMENT_CONNECTION_DEFINITION = "connection-definition";

   /**
    * connection-impl-class
    */
   public static final String ELEMENT_CONNECTION_IMPL_CLASS = "connection-impl-class";

   /**
    * connection-interface
    */
   public static final String ELEMENT_CONNECTION_INTERFACE = "connection-interface";

   /**
    * connector
    */
   public static final String ELEMENT_CONNECTOR = "connector";

   /**
    * credential-interface
    */
   public static final String ELEMENT_CREDENTIAL_INTERFACE = "credential-interface";

   /**
    * description
    */
   public static final String ELEMENT_DESCRIPTION = "description";

   /**
    * display-name
    */
   public static final String ELEMENT_DISPLAY_NAME = "display-name";

   /**
    * eis-type
    */
   public static final String ELEMENT_EIS_TYPE = "eis-type";

   /**
    * icon
    */
   public static final String ELEMENT_ICON = "icon";

   /**
    * inbound-resourceadapter
    */
   public static final String ELEMENT_INBOUND_RESOURCEADAPTER = "inbound-resourceadapter";

   /**
    * large-icon
    */
   public static final String ELEMENT_LARGE_ICON = "large-icon";

   /**
    * license
    */
   public static final String ELEMENT_LICENSE = "license";

   /**
    * license-required
    */
   public static final String ELEMENT_LICENSE_REQUIRED = "license-required";

   /**
    * managedconnectionfactory-class
    */
   public static final String ELEMENT_MANAGEDCONNECTIONFACTORY_CLASS = "managedconnectionfactory-class";

   /**
    * messageadapter
    */
   public static final String ELEMENT_MESSAGEADAPTER = "messageadapter";

   /**
    * messagelistener
    */
   public static final String ELEMENT_MESSAGELISTENER = "messagelistener";

   /**
    * messagelistener-type
    */
   public static final String ELEMENT_MESSAGELISTENER_TYPE = "messagelistener-type";

   /**
    * module-name
    */
   public static final String ELEMENT_MODULE_NAME = "module-name";

   /**
    * outbound-resourceadapter
    */
   public static final String ELEMENT_OUTBOUND_RESOURCEADAPTER = "outbound-resourceadapter";

   /**
    * reauthentication-support
    */
   public static final String ELEMENT_REAUTHENTICATION_SUPPORT = "reauthentication-support";

   /**
    * required-config-property
    */
   public static final String ELEMENT_REQUIRED_CONFIG_PROPERTY = "required-config-property";

   /**
    * required-work-context
    */
   public static final String ELEMENT_REQUIRED_WORK_CONTEXT = "required-work-context";

   /**
    * resourceadapter
    */
   public static final String ELEMENT_RESOURCEADAPTER = "resourceadapter";

   /**
    * resourceadapter-class
    */
   public static final String ELEMENT_RESOURCEADAPTER_CLASS = "resourceadapter-class";

   /**
    * resourceadapter-version
    */
   public static final String ELEMENT_RESOURCEADAPTER_VERSION = "resourceadapter-version";

   /**
    * security-permission
    */
   public static final String ELEMENT_SECURITY_PERMISSION = "security-permission";

   /**
    * security-permission-spec
    */
   public static final String ELEMENT_SECURITY_PERMISSION_SPEC = "security-permission-spec";

   /**
    * small-icon
    */
   public static final String ELEMENT_SMALL_ICON = "small-icon";

   /**
    * spec-version
    */
   public static final String ELEMENT_SPEC_VERSION = "spec-version";

   /**
    * transaction-support
    */
   public static final String ELEMENT_TRANSACTION_SUPPORT = "transaction-support";

   /**
    * vendor-name
    */
   public static final String ELEMENT_VENDOR_NAME = "vendor-name";

   /**
    * version
    */
   public static final String ELEMENT_VERSION = "version";
}
