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
package org.ironjacamar.common.metadata.spec;

import org.ironjacamar.common.api.metadata.spec.Connector;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JCA 1.5 tests
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class JCA15TestCase
{
   /**
    * Read
    * @throws Exception In case of an error
    */
   @Test
   public void testRead() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is);

      Connector c = parser.parse(is);
      assertNotNull(c);

      is.close();
   }

   /**
    * Write
    * @throws Exception In case of an error
    */
   @Test
   public void testWrite() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is = JCA16TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is);

      Connector c = parser.parse(is);
      assertNotNull(c);

      is.close();

      StringReader sr = new StringReader(c.toString());
      XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(sr);
      Connector cn = parser.parse(xsr);

      assertEquals(c, cn);
   }

   /**
    * Equal
    * @throws Exception In case of an error
    */
   @Test
   public void testEqual() throws Exception
   {
      RaParser parser = new RaParser();

      InputStream is1 = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is1);

      Connector c1 = parser.parse(is1);
      assertNotNull(c1);

      is1.close();

      InputStream is2 = JCA15TestCase.class.getClassLoader().
         getResourceAsStream("../../resources/test/spec/ra-1.5.xml");
      assertNotNull(is2);

      Connector c2 = parser.parse(is2);
      assertNotNull(c2);

      is2.close();

      assertEquals(c1, c2);
   }
}
