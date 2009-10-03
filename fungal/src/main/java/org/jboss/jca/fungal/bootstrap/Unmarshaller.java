/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.fungal.bootstrap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Unmarshaller for bootstrap.xml
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class Unmarshaller
{
   /**
    * Constructor
    */
   public Unmarshaller()
   {
   }

   /**
    * Unmarshal
    * @param url The URL
    * @return The result
    * @exception IOException If an I/O error occurs
    */
   public Bootstrap unmarshal(URL url) throws IOException
   {
      if (url == null)
         throw new IllegalArgumentException("URL is null");

      InputStream is = null;
      try
      {
         Bootstrap bootstrap = new Bootstrap();

         if ("file".equals(url.getProtocol()))
         {
            File file = new File(url.toURI());
            is = new FileInputStream(file);
         }
         else if ("jar".equals(url.getProtocol()))
         {
            JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
            is = jarConnection.getInputStream();
         }
         else
         {
            throw new IOException("Unsupport protocol: " + url);
         }

         is = new BufferedInputStream(is, 4096);

         XMLInputFactory xmlInputFactory = null;

         try
         {
            xmlInputFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory",
                                                          Thread.currentThread().getContextClassLoader());
         }
         catch (Throwable t)
         {
            xmlInputFactory = XMLInputFactory.newInstance();
         }

         XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);

         while (xmlStreamReader.hasNext())
         {
            int eventCode = xmlStreamReader.next();

            switch (eventCode)
            {
               case XMLStreamReader.START_ELEMENT :

                  if ("url".equals(xmlStreamReader.getLocalName()))
                     bootstrap.getUrl().add(readUrl(xmlStreamReader));

                  break;
               default :
            }
         }

         return bootstrap;
      }
      catch (Throwable t)
      {
         throw new IOException(t.getMessage(), t);
      }
      finally
      {
         try
         {
            if (is != null)
               is.close();
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }

   /**
    * Read: <url>
    * @param xmlStreamReader The XML stream
    * @return The result
    * @exception XMLStreamException Thrown if an error occurs
    */
   private String readUrl(XMLStreamReader xmlStreamReader) throws XMLStreamException
   {
      String result = null;

      int eventCode = xmlStreamReader.next();

      while (eventCode != XMLStreamReader.END_ELEMENT)
      {
         switch (eventCode)
         {
            case XMLStreamReader.CHARACTERS :
               result = xmlStreamReader.getText();
               break;
            default : 
         }
         eventCode = xmlStreamReader.next();
      }

      if (!"url".equals(xmlStreamReader.getLocalName()))
         throw new XMLStreamException("url tag not completed");

      return result;
   }
}
