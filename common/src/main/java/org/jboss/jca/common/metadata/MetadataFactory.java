/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.common.metadata;

import org.jboss.jca.common.CommonLogger;
import org.jboss.jca.common.api.metadata.ironjacamar.IronJacamar;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.metadata.ironjacamar.v11.IronJacamarParser;
import org.jboss.jca.common.metadata.ra.RaParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.jboss.logging.Logger;

/**
 *
 * A MetadataFactory.
 *
 * @author <a href="stefano.maestri@jboss.com">Stefano Maestri</a>
 *
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

            result = (new RaParser()).parse(input);

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
    * Get the JBoss specific metadata
    * @param root The root of the deployment
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public IronJacamar getIronJacamarMetaData(File root) throws Exception
   {
      IronJacamar result = null;

      File metadataFile = new File(root, "/META-INF/ironjacamar.xml");

      if (metadataFile.exists())
      {
         InputStream input = null;
         String url = metadataFile.getAbsolutePath();
         try
         {
            long start = System.currentTimeMillis();

            input = new FileInputStream(metadataFile);
            result = (new IronJacamarParser()).parse(input);

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
