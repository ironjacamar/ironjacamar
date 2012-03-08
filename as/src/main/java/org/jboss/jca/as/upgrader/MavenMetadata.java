/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.upgrader;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * maven-metadata.xml utilities
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MavenMetadata
{
   /**
    * Get the version from the specified url
    * @param url The url
    * @return The value
    * @exception IOException Thrown if the version can't be resolved
    */
   public static String getVersion(String url) throws IOException
   {
      try
      {
         Http http = new Http();
         String mavenMetadataXml = http.get(url);

         StringReader sr = new StringReader(mavenMetadataXml);

         XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
         XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(sr);

         String version = null;

         while (version == null && xmlStreamReader.hasNext())
         {
            int eventCode = xmlStreamReader.next();

            switch (eventCode)
            {
               case XMLStreamReader.START_ELEMENT :

                  if ("value".equals(xmlStreamReader.getLocalName()))
                  {
                     version = readString(xmlStreamReader);
                  }

                  break;
               default :
            }
         }

         if (version != null)
            return version;
      }
      catch (Throwable t)
      {
         throw new IOException("Unable to read: " + url, t);
      }

      throw new IOException("Unable to read: " + url);
   }

   /**
    * Read a string
    * @param xmlStreamReader The XML stream
    * @return The parameter
    * @exception XMLStreamException Thrown if an exception occurs
    */
   private static String readString(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      String result = null;

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               if (!xmlStreamReader.getText().trim().equals(""))
                  result = xmlStreamReader.getText().trim();

               break;

            default :
         }

         eventCode = xmlStreamReader.next();
      }

      return result;
   }
}
