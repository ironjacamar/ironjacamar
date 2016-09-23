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

package org.ironjacamar.common;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 * The common bundle.
 *
 * Message ids ranging from 010000 to 019999 inclusively.
 */
@MessageBundle(projectCode = "IJ2")
public interface CommonBundle
{
   /**
    * Annotation repository is null
    * @return The value
    */
   @Message(id = 10051, value = "AnnotationRepository reference is null")
   public String annotationRepositoryNull();

   /**
    * No @Connector defined
    * @return The value
    */
   @Message(id = 10052, value = "No @Connector defined")
   public String noConnectorDefined();

   /**
    * More than @Connector defined
    * @return The value
    */
   @Message(id = 10053, value = "More than @Connector defined")
   public String moreThanOneConnectorDefined();

   /**
    * More than one @ConnectionDefinitions defined
    * @return The value
    */
   @Message(id = 10054, value = "More than one @ConnectionDefinitions defined")
   public String moreThanOneConnectionDefinitionsDefined();

   /**
    * Unknown annotation
    * @param annotation The annotation
    * @return The value
    */
   @Message(id = 10055, value = "Unknown annotation: %s")
   public String unknownAnnotation(Object annotation);

   /**
    * Element as boolean
    * @param value The value
    * @param element The element
    * @return The value
    */
   @Message(id = 10056, value = "%s isn't a valid boolean for element %s. "
         + "We accept only \"true\" or \"false\" as boolean value")
   public String elementAsBoolean(String value, String element);

   /**
    * Attribute as boolean
    * @param value The value
    * @param element The element
    * @return The value
    */
   @Message(id = 10057, value = "%s isn't a valid boolean for attribute %s. "
         + "We accept only \"true\" or \"false\" as boolean value")
   public String attributeAsBoolean(String value, String element);

   /**
    * Not a valid number
    * @param value The value
    * @param element The element
    * @return The value
    */
   @Message(id = 10058, value = "%s isn't a valid number for element %s")
   public String notValidNumber(String value, String element);

   /**
    * Not a valid flush strategy
    * @param value The value
    * @return The value
    */
   @Message(id = 10059, value = "%s isn't a valid flush strategy")
   public String notValidFlushStrategy(String value);

   /**
    * Unexpected end tag
    * @param value The value
    * @return The value
    */
   @Message(id = 10060, value = "Unexpected end tag: %s")
   public String unexpectedEndTag(String value);

   /**
    * Unexpected element
    * @param value The value
    * @return The value
    */
   @Message(id = 10061, value = "Unexpected element: %s")
   public String unexpectedElement(String value);

   /**
    * Unexpected end of document
    * @return The value
    */
   @Message(id = 10062, value = "Reached end of xml document unexpectedly")
   public String unexpectedEndOfDocument();

   /**
    * Missing class name
    * @param value The value
    * @return The value
    */
   @Message(id = 10063, value = "Mandatory class-name attribute missing in: %s")
   public String missingClassName(String value);

   /**
    * Unexpected attribute
    * @param value The value
    * @param location The location
    * @return The value
    */
   @Message(id = 10064, value = "Unexpected attribute %s at %s")
   public String unexpectedAttribute(String value, String location);

   /**
    * Missing jndi name
    * @param value The value
    * @return The value
    */
   @Message(id = 10065, value = "Missing mandatory jndi-name attribute: %s")
   public String missingJndiName(String value);

   /**
    * Multiple pools
    * @return The value
    */
   @Message(id = 10066, value = "You cannot define more than one pool or xa-pool in same connection-definition")
   public String multiplePools();

   /**
    * Unsupported element
    * @param value The value
    * @return The value
    */
   @Message(id = 10067, value = "Element %s cannot be set without an xa-pool")
   public String unsupportedElement(String value);

   /**
    * Required attribute
    * @param attr The attribute
    * @param tag The tag
    * @return The value
    */
   @Message(id = 10068, value = "Missing required attribute %s in %s")
   public String requiredAttributeMissing(String attr, String tag);

   /**
    * Required element
    * @param element The element
    * @param tag The tag
    * @return The value
    */
   @Message(id = 10069, value = "Missing required element %s in %s")
   public String requiredElementMissing(String element, String tag);

   /**
    * Invalid negative
    * @param tag The tag
    * @return The value
    */
   @Message(id = 10070, value = "Invalid negative value for %s")
   public String invalidNegative(String tag);

   /**
    * Invalid tag
    * @param tag The tag
    * @return The value
    */
   @Message(id = 10071, value = "%s is not valid. See exception for more details")
   public String invalidTag(String tag);

   /**
    * Null value
    * @param tag The tag
    * @return The value
    */
   @Message(id = 10072, value = "%s cannot be undefined")
   public String nullValue(String tag);

   /**
    * Invalid security configuration
    * @return The value
    */
   @Message(id = 10073, value = "Invalid <security> configuration")
   public String invalidSecurityConfiguration();

   /**
    * Resource adapter not defined
    * @return The value
    */
   @Message(id = 10074, value = "The resource adapter metadata must be defined")
   public String noMetadataForResourceAdapter();

   /**
    * Invalid metadata for resource adapter 
    * @return The value
    */
   @Message(id = 10075, value = "The resource adapter metadata must contain " +
         "either an outbound or inbound configuration")
   public String invalidMetadataForResourceAdapter();

   /**
    * Missing value
    * @param tag The tag
    * @return The value
    */
   @Message(id = 10076, value = "%s must be defined")
   public String missingValue(String tag);

   /**
    * Wrong annotation type
    * @param annotation The annotation
    * @return The value
    */
   @Message(id = 10077, value = "Wrong annotation type: %s")
   public String wrongAnnotationType(Object annotation);

   /**
    * AnnotationClass should be an annotation
    * @return The value
    */
   @Message(id = 10078, value = "AnnotationClass should be an annotation")
   public String annotationClassShouldBeAnnotation();

   /**
    * Repository cannot be null
    * @return The value
    */
   @Message(id = 10079, value = "repository cannot be null")
   public String repositoryNull();

   /**
    * fullQualifiedName should be one of...
    * @return The value
    */
   @Message(id = 10080, value = "fullQualifiedName should be one of "
         + "javax.resource.spi.security.PasswordCredential; org.ietf.jgss.GSSCredential;"
         + " javax.resource.spi.security.GenericCredential")
   public String fullQualifiedName();

   /**
    * Wrong version
    * @param version The version
    * @return The value
    */
   @Message(id = 10081, value = "version= %s")
   public String wrongVersion(Object version);

   /**
    * no annotation scanner
    *
    * @return The value
    */
   @Message(id = 10082, value = "Unable to find an annotation scanner implementation")
   public String noAnnotationScanner();
}
