/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.embedded.deployers;

/**
 * Constants
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class Constants
{
   /** DEPLOYER_RAR_FILE_EXTRACTOR */
   public static final int DEPLOYER_RAR_FILE_EXTRACTOR = 0;

   /** DEPLOYER_CLASSLOADER */
   public static final int DEPLOYER_CLASSLOADER = 1;

   /** DEPLOYER_RA_XML_METADATA */
   public static final int DEPLOYER_RA_XML_METADATA = 2;

   /** DEPLOYER_ANNOTATIONS */
   public static final int DEPLOYER_ANNOTATIONS = 3;

   /** DEPLOYER_VALIDATE_METADATA */
   public static final int DEPLOYER_VALIDATE_METADATA = 4;

   /** DEPLOYER_METADATA */
   public static final int DEPLOYER_METADATA = 5;

   /** DEPLOYER_IRONJACAMAR_XML */
   public static final int DEPLOYER_IRONJACAMAR_XML = 6;

   /** DEPLOYER_DASH_RA_XML */
   public static final int DEPLOYER_DASH_RA_XML = 7;

   /** ATTACHMENT_ARCHIVE */
   public static final String ATTACHMENT_ARCHIVE = "archive";

   /** ATTACHMENT_CLASSLOADER */
   public static final String ATTACHMENT_CLASSLOADER = "classloader";

   /** ATTACHMENT_RA_XML_METADATA */
   public static final String ATTACHMENT_RA_XML_METADATA = "raxml";

   /** ATTACHMENT_MERGED_METADATA */
   public static final String ATTACHMENT_MERGED_METADATA = "merged";

   /**
    * Constructor
    */
   private Constants()
   {
   }
}
