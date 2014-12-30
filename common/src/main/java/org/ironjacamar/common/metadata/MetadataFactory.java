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

package org.ironjacamar.common.metadata;

import org.ironjacamar.common.CommonLogger;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.metadata.ironjacamar.IronJacamarParser;
import org.ironjacamar.common.metadata.spec.RaParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jboss.logging.Logger;

/**
 * A MetadataFactory.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class MetadataFactory
{
   private static CommonLogger log = Logger.getMessageLogger(CommonLogger.class, MetadataFactory.class.getName());

   /**
    * Constructor
    */
   public MetadataFactory()
   {
   }

   /**
    * Get the JCA standard metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public Connector getStandardMetaData(File root) throws Exception
   {
      Connector result = null;
      File metadataFile = new File(root, "/META-INF/ra.xml");

      if (metadataFile.exists())
      {
         InputStream input = null;
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();
            input = new FileInputStream(metadataFile);

            XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(input);

            result = (new RaParser()).parse(xsr);

            log.debugf("Total parse for %s took %d ms", url, (System.currentTimeMillis() - start));
         }
         catch (Exception e)
         {
            log.parsingErrorRaXml(url, e);
            throw e;
         }
         finally
         {
            if (input != null)
               input.close();
         }

      }
      else
      {
         log.tracef("metadata file %s does not exist", metadataFile.toString());
      }

      return result;
   }

   /**
    * Get the IronJacamar specific metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public Activation getIronJacamarMetaData(File root) throws Exception
   {
      Activation result = null;

      File metadataFile = new File(root, "/META-INF/ironjacamar.xml");

      if (metadataFile.exists())
      {
         InputStream input = null;
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            input = new FileInputStream(metadataFile);

            XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(input);

            result = (new IronJacamarParser()).parse(xsr);

            log.debugf("Total parse for %s took %d ms", url, (System.currentTimeMillis() - start));
         }
         catch (Exception e)
         {
            log.parsingErrorIronJacamarXml(url, e);
            throw e;
         }
         finally
         {
            if (input != null)
               input.close();
         }
      }

      return result;
   }
}
