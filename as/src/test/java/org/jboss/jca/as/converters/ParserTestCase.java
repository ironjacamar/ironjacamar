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

package org.jboss.jca.as.converters;

import java.io.InputStream;

import org.jboss.logging.Logger;

//import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A JCA16AnnoProfile test case.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ParserTestCase
{
   private static Logger log = Logger.getLogger(ParserTestCase.class);
   
   private String[] xaFilesName = {
      "db2-jcc-xa-ds.xml",
      "informix-xa-ds.xml",
      "mysql-xa-ds.xml",
      "postgres-xa-ds.xml",
      "db2-xa-ds.xml",
      "mimer-xa-ds.xml",
      "oracle-xa-ds.xml",
      "sybase-xa-ds.xml",
      "derby-xa-ds.xml",
      "mssql-xa-ds.xml",
      "pointbase-xa-ds.xml"
   };
   
   private String[] dsFilesName = {
      "hsqldb-ds.xml",
      "hsqldb-encrypted-ds.xml",
      "oracle-ds.xml",
      "db2-400-ds.xml",
      "informix-ds.xml",
      "db2-ds.xml",
      "pointbase-ds.xml",
      "jdatastore-ds.xml",
      "postgres-ds.xml",
      "derby-ds.xml",
      "jsql-ds.xml",
      "mimer-ds.xml",
      "progress-ds.xml",
      "sapdb-ds.xml",
      "msaccess-ds.xml",
      "mssql-ds.xml",
      "solid-ds.xml",
      "sybase-ds.xml",
      "mysql-ds.xml"
   };
   
   private String[] cfFilesName = {
      "asapxcess-jb3.2-ds.xml",
      "cicsr9s-ds.xml",
      "jms-ds.xml",
      "facets-ds.xml",
      "fastobjects-jboss32-ds.xml",
      "sapr3-ds.xml",
      "firebird-ds.xml",
      "hajndi-jms-ds.xml",
   };

   /**
    * test xa ds parser
    * @throws Throwable throwable exception 
    */
   @Test
   public void testXaDsParser() throws Throwable
   {
      LegacyDsParser parser = new LegacyDsParser();
      
      for (String xaFileName : xaFilesName)
      {
         System.out.println("\nStart parse... " + xaFileName);
         InputStream in = ParserTestCase.class.getClassLoader().getResourceAsStream("ds/" + xaFileName);
         DataSources ds = parser.parse(in);
   
         if (ds != null)
            System.out.println(ds.toString());
      }
   }
   
   /**
    * test local tx ds parser
    * @throws Throwable throwable exception 
    */
   @Test
   public void testLocalTxDsParser() throws Throwable
   {
      LegacyDsParser parser = new LegacyDsParser();
      
      for (String dsFileName : dsFilesName)
      {
         System.out.println("\nStart parse... " + dsFileName);
         InputStream in = ParserTestCase.class.getClassLoader().getResourceAsStream("ds/" + dsFileName);
         DataSources ds = parser.parse(in);
         
         if (ds != null)
            System.out.println(ds.toString());
      }
   }
   
   /**
    * test cf parser
    * @throws Throwable throwable exception 
    */
   @Test
   public void testCfParser() throws Throwable
   {
      LegacyCfParser parser = new LegacyCfParser();
      
      for (String cfFileName : cfFilesName)
      {
         System.out.println("\nStart parse... " + cfFileName);
         InputStream in = ParserTestCase.class.getClassLoader().getResourceAsStream("ds/" + cfFileName);
         ConnectionFactories ds = parser.parse(in);
   
         if (ds != null)
            System.out.println(ds.toString());
      }
   }
}
