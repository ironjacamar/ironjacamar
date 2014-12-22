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
package org.ironjacamar.common.metadata.ds;

import org.ironjacamar.common.api.metadata.ds.DataSources;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DataSources 1.3 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class DataSources13TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is = DataSources13TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.3.xml");
      assertNotNull(is);

      DataSources ds = parser.parse(is);
      assertNotNull(ds);

      is.close();
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is = DataSources13TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.3.xml");
      assertNotNull(is);

      DataSources ds = parser.parse(is);
      assertNotNull(ds);

      is.close();

      StringReader sr = new StringReader(ds.toString());
      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      DataSources dsn = parser.parse(xsr);

      assertEquals(ds, dsn);
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      DsParser parser = new DsParser();

      InputStream is1 = DataSources13TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.3.xml");
      assertNotNull(is1);

      DataSources ds1 = parser.parse(is1);
      assertNotNull(ds1);

      is1.close();

      InputStream is2 = DataSources13TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/ds/dashds-1.3.xml");
      assertNotNull(is2);

      DataSources ds2 = parser.parse(is2);
      assertNotNull(ds2);

      is2.close();

      assertEquals(ds1, ds2);
   }
}
