/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008-2009, Red Hat Inc, and individual contributors
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
package org.jboss.jca.core.mdr;

import org.jboss.jca.common.api.metadata.resourceadapter.Activation;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.core.spi.mdr.AlreadyExistsException;
import org.jboss.jca.core.spi.mdr.MetadataRepository;
import org.jboss.jca.core.spi.mdr.NotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the SimpleMetadataRepository
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a> 
 */
public class MetadataRepositoryTestCase
{
   /**
    * Test: Constructor
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testConstructor() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();
   }

   /**
    * Test: RegisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterResourceAdapterNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(null, root, md, a);
   }

   /**
    * Test: RegisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterResourceAdapterEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);
   }

   /**
    * Test: RegisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterResourceAdapterNullFile() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = null;
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);
   }

   /**
    * Test: RegisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterResourceAdapterNullConnector() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = null;
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);
   }

   /**
    * Test: RegisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = AlreadyExistsException.class)
   public void testRegisterResourceAdapterAlreadyExists() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);
      mdr.registerResourceAdapter(uniqueId, root, md, a);
   }

   /**
    * Test: RegisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testRegisterResourceAdapter() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);
   }

   /**
    * Test: UnregisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterResourceAdapterNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      mdr.unregisterResourceAdapter(uniqueId);
   }

   /**
    * Test: UnregisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterResourceAdapterEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      mdr.unregisterResourceAdapter(uniqueId);
   }

   /**
    * Test: UnregisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = NotFoundException.class)
   public void testUnregisterResourceAdapterNotFoundException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";

      mdr.unregisterResourceAdapter(uniqueId);
   }

   /**
    * Test: UnregisterResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testUnregisterResourceAdapter() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);
      mdr.unregisterResourceAdapter(uniqueId);
   }

   /**
    * Test: GetResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetResourceAdapterNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      Connector connector = mdr.getResourceAdapter(uniqueId);
   }

   /**
    * Test: GetResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetResourceAdapterEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      Connector connector = mdr.getResourceAdapter(uniqueId);
   }

   /**
    * Test: GetResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = NotFoundException.class)
   public void testGetResourceAdapterNotFoundException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";

      Connector connector = mdr.getResourceAdapter(uniqueId);
   }

   /**
    * Test: GetResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testGetResourceAdapter() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);

      Connector cmd = mdr.getResourceAdapter(uniqueId);

      assertFalse(md.equals(cmd));
   }

   /**
    * Test: GetResourceAdapters
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = UnsupportedOperationException.class)
   public void testGetResourceAdaptersUnsupportedOperationException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      Set<String> ids = mdr.getResourceAdapters();
      assertNotNull(ids);
      assertEquals(ids.size(), 0);

      ids.add("KEY");
   }

   /**
    * Test: GetResourceAdapters
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testGetResourceAdapters() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      Set<String> ids = mdr.getResourceAdapters();
      assertNotNull(ids);
      assertEquals(ids.size(), 0);

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);

      ids = mdr.getResourceAdapters();
      assertNotNull(ids);
      assertEquals(ids.size(), 1);
      assertTrue(ids.contains("KEY"));
   }

   /**
    * Test: GetRoot
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetRootNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      File root = mdr.getRoot(uniqueId);
   }

   /**
    * Test: GetRoot
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetRootEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      File root = mdr.getRoot(uniqueId);
   }

   /**
    * Test: GetRoot
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = NotFoundException.class)
   public void testGetRootNotFoundException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";

      File root = mdr.getRoot(uniqueId);
   }

   /**
    * Test: GetRoot
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testGetRoot() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);

      File f = mdr.getRoot(uniqueId);
      assertEquals(root, f);
   }

   /**
    * Test: GetIronJacamar
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetIronJacamarNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      Activation a = mdr.getActivation(uniqueId);
   }

   /**
    * Test: GetIronJacamar
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetIronJacamarEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      Activation a = mdr.getActivation(uniqueId);
   }

   /**
    * Test: GetIronJacamar
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = NotFoundException.class)
   public void testGetIronJacamarNotFoundException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";

      Activation a = mdr.getActivation(uniqueId);
   }

   /**
    * Test: GetIronJacamar
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testGetIronJacamar() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "KEY";
      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);

      Activation a2 = mdr.getActivation(uniqueId);
      assertEquals(a, a2);
   }

   /**
    * Test: HasResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testHasResourceAdapterNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      boolean result = mdr.hasResourceAdapter(uniqueId);
   }

   /**
    * Test: HasResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testHasResourceAdapterEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      boolean result = mdr.hasResourceAdapter(uniqueId);
   }

   /**
    * Test: HasResourceAdapter
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testHasResourceAdapter() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";

      assertFalse(mdr.hasResourceAdapter(uniqueId));

      File root = new File(".");
      Connector md = mock(Connector.class);
      Activation a = mock(Activation.class);

      mdr.registerResourceAdapter(uniqueId, root, md, a);

      assertTrue(mdr.hasResourceAdapter(uniqueId));
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterJndiMappingNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;
      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterJndiMappingEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";
      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterJndiMappingNullClz() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = null;
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterJndiMappingEmptyClz() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterJndiMappingNullJndi() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = null;

      mdr.registerJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testRegisterJndiMappingEmptyJndi() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = "";

      mdr.registerJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: RegisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testRegisterJndiMapping() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);

      String clz2 = "class2";
      String jndi2 = "mapping2";

      mdr.registerJndiMapping(uniqueId, clz2, jndi2);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterJndiMappingNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;
      String clz = "class";
      String jndi = "mapping";

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterJndiMappingEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";
      String clz = "class";
      String jndi = "mapping";

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterJndiMappingNullClz() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = null;
      String jndi = "mapping";

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterJndiMappingEmptyClz() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "";
      String jndi = "mapping";

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterJndiMappingNullJndi() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = null;

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testUnregisterJndiMappingEmptyJndi() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = "";

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
   }

   /**
    * Test: UnregisterJndiMapping
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testUnregisterJndiMapping() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);

      String clz2 = "class2";
      String jndi2 = "mapping2";

      mdr.registerJndiMapping(uniqueId, clz2, jndi2);

      mdr.unregisterJndiMapping(uniqueId, clz, jndi);
      mdr.unregisterJndiMapping(uniqueId, clz2, jndi2);
   }

   /**
    * Test: HasJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testHasJndiMappingsNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      boolean result = mdr.hasJndiMappings(uniqueId);
   }

   /**
    * Test: HasJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testHasJndiMappingsEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      boolean result = mdr.hasJndiMappings(uniqueId);
   }

   /**
    * Test: HasJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testHasJndiMappings() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";

      assertFalse(mdr.hasJndiMappings(uniqueId));

      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);

      assertTrue(mdr.hasJndiMappings(uniqueId));
   }

   /**
    * Test: GetJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetJndiMappingsNullUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = null;

      Map<String, List<String>> mappings = mdr.getJndiMappings(uniqueId);
   }

   /**
    * Test: GetJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = IllegalArgumentException.class)
   public void testGetJndiMappingsEmptyUniqueId() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "";

      Map<String, List<String>> mappings = mdr.getJndiMappings(uniqueId);
   }

   /**
    * Test: GetJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = NotFoundException.class)
   public void testGetJndiMappingsNotFoundException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";

      Map<String, List<String>> mappings = mdr.getJndiMappings(uniqueId);
   }

   /**
    * Test: GetJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test(expected = UnsupportedOperationException.class)
   public void testGetJndiMappingsUnsupportedOperationException() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);

      Map<String, List<String>> mappings = mdr.getJndiMappings(uniqueId);
      assertNotNull(mappings);
      assertEquals(mappings.size(), 1);

      List<String> l = new ArrayList<String>(1);
      l.add("value");
      mappings.put("hack", l);
   }

   /**
    * Test: GetJndiMappings
    * @exception Throwable Thrown in case of an error
    */
   @Test
   public void testGetJndiMappings() throws Throwable
   {
      MetadataRepository mdr = new SimpleMetadataRepository();

      String uniqueId = "key";
      String clz = "class";
      String jndi = "mapping";

      mdr.registerJndiMapping(uniqueId, clz, jndi);

      Map<String, List<String>> mappings = mdr.getJndiMappings(uniqueId);
      assertNotNull(mappings);
      assertEquals(mappings.size(), 1);
   }
}
