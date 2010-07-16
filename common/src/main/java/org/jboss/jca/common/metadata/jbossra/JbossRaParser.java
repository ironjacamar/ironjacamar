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
package org.jboss.jca.common.metadata.jbossra;

import org.jboss.jca.common.metadata.MetadataParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jboss.as.model.Domain;
import org.jboss.as.model.Element;

/**
 * A JbossRaParser.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
 */
public class JbossRaParser implements MetadataParser<JbossRa>
{


   /**
    * Parse the xml file and return the {@link JbossRa} metadata
    * @param xmlFile The xml file to parse
    * @return The {@link JbossRa} metadata
    * @exception Exception Thrown if an error occurs
    */
   @Override
   public JbossRa parse(File xmlFile) throws Exception
   {
      XMLInputFactory inputFactory=XMLInputFactory.newInstance();
      InputStream input=new FileInputStream(xmlFile);
      XMLStreamReader  reader =inputFactory.createXMLStreamReader(input);

      while (reader.hasNext()) {
         switch (reader.nextTag()) {
             case END_ELEMENT: {
                 // should mean we're done, so ignore it.
                 break;
             }
             case START_ELEMENT: {
                 if (Domain.NAMESPACES.contains(reader.getNamespaceURI())) {
                     switch (Element.forName(reader.getLocalName())) {
                         case SERVER_GROUPS: {
                             readServerGroupsElement(reader, domain);
                             break;
                         }
                         default: throw unexpectedElement(reader);
                     }
                 } else {
                     // handle foreign root elements
                     reader.handleAny(domain);
                 }
                 break;
             }
             default: throw new IllegalStateException();
         }
     }

   }

}
