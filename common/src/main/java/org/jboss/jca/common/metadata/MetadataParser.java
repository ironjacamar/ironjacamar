/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jca.common.metadata;

import java.io.File;

public interface MetadataParser<T extends JCAMetadata>
{

   /**
    * Get the JCAMetaData for the file parser is designed
    * @param xmlFile The xml file to parse
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public T parse(File xmlFile) throws Exception;

}
