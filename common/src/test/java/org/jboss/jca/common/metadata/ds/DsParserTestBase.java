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
package org.jboss.jca.common.metadata.ds;

import org.jboss.jca.common.api.metadata.ds.DataSources;
import org.jboss.jca.common.metadata.ds.v10.DsParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.jboss.logging.Logger;

import org.junit.BeforeClass;

import static org.junit.Assert.*;

/**
 * A base class for DS parsing tests
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */

public abstract class DsParserTestBase
{

   /**
    * parser
    */
   protected static DsParser parser;

   /**
    *  logger
    */
   protected static Logger log = Logger.getLogger(DsParserTestBase.class);

   /**
    *
    * beforeClass method
    *
    * @throws Exception in casae of file not found
    */
   @BeforeClass
   public static void beforeClass() throws Exception
   {
      parser = new DsParser();
   }

   /**
    * gets DataSources from file
    * 
    * @param xmlFile - to parse
    * 
    * @return resulting DS
    * 
    * @throws Exception in case of error
    */
   protected DataSources doParse(File xmlFile) throws Exception
   {
      FileInputStream is = null;

      try
      {
         is = new FileInputStream(xmlFile);
         return parser.parse(is);
      }
      finally
      {
         if (is != null)
            is.close();
      }
   }

   /**
    * Parses DS, then tries to re-parse it, using its xml representation, 
    * compares resulting DS with re-parsed one
    * 
    * @param xmlFile to parse
    * @return resulting DS 
    *
    * @throws Exception in case of error
    */
   protected DataSources doReParse(File xmlFile) throws Exception
   {
      ByteArrayInputStream bais = null;
      try
      {
         DataSources ds = doParse(xmlFile);
         String xmlRepresentation = ds.toString();
         bais = new ByteArrayInputStream(xmlRepresentation.getBytes("UTF-8"));
         DataSources ds1 = parser.parse(bais);
         assertEquals("Strings are not equal.\n" + xmlRepresentation + "\n" + ds1.toString(), xmlRepresentation,
               ds1.toString());
         assertTrue("DS Objects are not equal:\n" + ds1.toString() + "\n" + ds.toString(), ds1.equals(ds));
         return ds;
      }
      finally
      {
         if (bais != null)
            bais.close();
      }
   }

   /**
    * Returns resource file by name
    * 
    * @param fileName in resource directory
    * @return File
    * @throws Exception in case of error
    */
   protected File getFile(String fileName) throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      return new File(cl.getResource(fileName).toURI());
   }

   /**
    * Returns DS object as a result of parsing file from resource folder
    * 
    * @param fileName to parse
    * 
    * @return resulting DS
    * 
    * @throws Exception in case of error
    */
   protected DataSources parseDsFromFile(String fileName) throws Exception
   {
      return doReParse(getFile(fileName));
   }
}
