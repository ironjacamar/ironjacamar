/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2016, Red Hat Inc, and individual contributors
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

package org.ironjacamar.rarinfo;

import org.ironjacamar.common.api.metadata.common.Credential;
import org.ironjacamar.common.api.metadata.common.Recovery;
import org.ironjacamar.common.api.metadata.common.TransactionSupportEnum;
import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.Activations;
import org.ironjacamar.common.api.metadata.resourceadapter.AdminObject;
import org.ironjacamar.common.api.metadata.resourceadapter.ConnectionDefinition;
import org.ironjacamar.common.api.metadata.spec.Connector;
import org.ironjacamar.common.metadata.resourceadapter.ResourceAdapterParser;
import org.ironjacamar.rars.ResourceAdapterFactory;
import org.ironjacamar.rars.lazy.LazyConnection;
import org.ironjacamar.rars.lazy.LazyConnectionFactory;
import org.ironjacamar.rars.lazy.LazyManagedConnectionFactory;
import org.ironjacamar.rars.lazy.LazyResourceAdapter;
import org.ironjacamar.rars.perf.PerfConnection;
import org.ironjacamar.rars.perf.PerfConnectionFactory;
import org.ironjacamar.rars.perf.PerfManagedConnectionFactory;
import org.ironjacamar.rars.security.UnifiedSecurityConnection;
import org.ironjacamar.rars.security.UnifiedSecurityConnectionFactory;
import org.ironjacamar.rars.security.UnifiedSecurityManagedConnectionFactory;
import org.ironjacamar.rars.security.UnifiedSecurityResourceAdapter;
import org.ironjacamar.rars.test.TestAdminObject;
import org.ironjacamar.rars.test.TestAdminObjectImpl;
import org.ironjacamar.rars.test.TestConnection;
import org.ironjacamar.rars.test.TestConnectionFactory;
import org.ironjacamar.rars.test.TestManagedConnectionFactory;
import org.ironjacamar.rars.test.TestResourceAdapter;
import org.ironjacamar.rars.test.inflow.TestActivationSpec;
import org.ironjacamar.rars.test.inflow.TestMessageListener;
import org.ironjacamar.rars.txlog.TxLogConnection;
import org.ironjacamar.rars.txlog.TxLogConnectionFactory;
import org.ironjacamar.rars.txlog.TxLogManagedConnectionFactory;
import org.ironjacamar.rars.wm.WorkConnection;
import org.ironjacamar.rars.wm.WorkConnectionFactory;
import org.ironjacamar.rars.wm.WorkManagedConnectionFactory;
import org.ironjacamar.rars.wm.WorkResourceAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.ironjacamar.rars.ResourceAdapterFactory.*;
import static org.junit.Assert.*;

/**
 * Test for rarinfo tool.
 *
 * @author Flavia Rainone
 */
public class RarInfoTestCase
{

   private static File raaFile;

   /**
    *
    */
   @BeforeClass
   public static void setupTempRarFile()
   {
      File parentDirectory = new File(SecurityActions.getSystemProperty("java.io.tmpdir"));
      raaFile = new File(parentDirectory, "rafile.rar");
      raaFile.deleteOnExit();
   }

   /**
    *
    */
   @After
   public void destroyTempRarFile()
   {
      assertTrue(raaFile.delete() || raaFile.exists());
   }

   /**
    * Tests rarInfo tool with lazy rar.
    *
    * @throws Exception in case of an error
    */
   @Test
   public void lazyRar() throws Exception
   {
      ResourceAdapterArchive raArchive = createLazyRar();
      raArchive.as(ZipExporter.class).exportTo(raaFile, true);
      Main.rarInfo(raaFile.getAbsolutePath(), null, false, "lazyrar-output.txt");
      final File file = new File("lazyrar-output.txt");
      assertTrue(file.exists());
      file.deleteOnExit();
      try (BufferedReader reader = new BufferedReader(new FileReader(file)))
      {
         assertConnector(reader, raaFile.getName(), Connector.Version.V_15, Main.ConnectorType.OUTBOUND,
               TransactionSupportEnum.XATransaction, false, true, false);
         assertStructure(reader, "META-INF/", "META-INF/ra.xml", "lazy.jar");

         // resource adapter
         assertResourceAdapter(reader, LazyResourceAdapter.class, new String[]{"Enable", "java.lang.Boolean"},
               new String[] {"LocalTransaction", "java.lang.Boolean"},
               new String[] {"XATransaction", "java.lang.Boolean"});

         assertManagedConnectionFactory(reader, LazyManagedConnectionFactory.class, false, true, false, "Unknown",
               "Unknown", false, LazyConnectionFactory.class, new String[] {
                  "LazyConnection getConnection() throws ResourceException",
                  "void setReference(Reference)", "Reference getReference() throws NamingException"},
               LazyConnection.class, new String[] {"boolean isManagedConnectionSet()",
                  "boolean closeManagedConnection()", "boolean enlist()", "boolean associate()",
                  "boolean isEnlisted()", "void close()" }, new String[0], new String[0]);

         // Contents of META-INF/ra.xml
         assertRAXml(reader, raArchive);

         // deployment descriptor
         assertDeploymentDescriptor(reader);
         Activations activations = parseDeploymentDescriptor(reader);
         assertNotNull(activations);
         for (Activation activation : activations.getActivations())
         {
            assertEquals(raaFile.getName(), activation.getArchive());
            Map<String, String> configProperties = activation.getConfigProperties();
            assertEquals(3, configProperties.size());
            assertTrue(configProperties.containsKey("Enable"));
            assertEquals("true", configProperties.get("Enable"));
            assertTrue(configProperties.containsKey("LocalTransaction"));
            assertEquals("false", configProperties.get("LocalTransaction"));
            assertTrue(configProperties.containsKey("XATransaction"));
            assertEquals("false", configProperties.get("XATransaction"));
            assertEquals(TransactionSupportEnum.XATransaction, activation.getTransactionSupport());
            List<ConnectionDefinition> connectionDefinitions = activation.getConnectionDefinitions();
            assertEquals(1, connectionDefinitions.size());
            ConnectionDefinition connectionDefinition = connectionDefinitions.get(0);
            assertEquals(LazyManagedConnectionFactory.class.getName(), connectionDefinition.getClassName());
            assertEquals("LazyConnection", connectionDefinition.getId());
            assertEquals("java:jboss/eis/LazyConnection", connectionDefinition.getJndiName());
            Recovery recovery = connectionDefinition.getRecovery();
            assertNotNull(recovery);
            Credential credential = recovery.getCredential();
            assertNotNull(credential);
            assertEquals("domain", credential.getSecurityDomain());
         }
      }
   }

   /**
    * Tests rarInfo tool with perf rar.
    *
    * @throws Exception in case of an error
    */
   @Test
   public void perfRar() throws Exception
   {
      ResourceAdapterArchive raArchive = ResourceAdapterFactory.createPerfRar();
      raArchive.as(ZipExporter.class).exportTo(raaFile, true);
      Main.rarInfo(raaFile.getAbsolutePath(), null, false, "perfrar-output.txt");
      final File file = new File("perfrar-output.txt");
      assertTrue(file.exists());
      file.deleteOnExit();
      try (BufferedReader reader = new BufferedReader(new FileReader(file)))
      {
         assertConnector(reader, raaFile.getName(), Connector.Version.V_15, Main.ConnectorType.OUTBOUND,
               TransactionSupportEnum.XATransaction, false, true, false);
         assertStructure(reader, "META-INF/", "META-INF/ra.xml", "perf.jar");

         // managed connection factory
         assertManagedConnectionFactory(reader, PerfManagedConnectionFactory.class, false, false, false, "Unknown",
               "Unknown", false, PerfConnectionFactory.class, new String[] {
                  "PerfConnection getConnection() throws ResourceException",
                  "void setReference(Reference)", "Reference getReference() throws NamingException"},
               PerfConnection.class, new String[] {"void close()", "void error()" },
               new String[0], new String[] {"txBeginDuration (java.lang.Long)", "txCommitDuration (java.lang.Long)"});

         // Contents of META-INF/ra.xml
         assertRAXml(reader, raArchive);

         // deployment descriptor
         assertDeploymentDescriptor(reader);
         Activations activations = parseDeploymentDescriptor(reader);
         assertNotNull(activations);
         for (Activation activation : activations.getActivations())
         {
            assertEquals(raaFile.getName(), activation.getArchive());
            assertNull(activation.getConfigProperties());
            assertEquals(TransactionSupportEnum.XATransaction, activation.getTransactionSupport());
            List<ConnectionDefinition> connectionDefinitions = activation.getConnectionDefinitions();
            assertEquals(1, connectionDefinitions.size());
            ConnectionDefinition connectionDefinition = connectionDefinitions.get(0);
            assertEquals(PerfManagedConnectionFactory.class.getName(), connectionDefinition.getClassName());
            assertEquals("PerfConnection", connectionDefinition.getId());
            assertEquals("java:jboss/eis/PerfConnection", connectionDefinition.getJndiName());
            Recovery recovery = connectionDefinition.getRecovery();
            assertNotNull(recovery);
            Credential credential = recovery.getCredential();
            assertNotNull(credential);
            assertEquals("domain", credential.getSecurityDomain());
         }
      }
   }

   /**
    * Tests rarInfo tool with test rar.
    *
    * @throws Exception in case of an error
    */
   @Test
   public void testRar() throws Exception
   {
      ResourceAdapterArchive raArchive = ResourceAdapterFactory.createTestRar();
      raArchive.as(ZipExporter.class).exportTo(raaFile, true);
      Main.rarInfo(raaFile.getAbsolutePath(), null, false, "testrar-output.txt");
      final File file = new File("testrar-output.txt");
      assertTrue(file.exists());
      file.deleteOnExit();
      try (BufferedReader reader = new BufferedReader(new FileReader(file)))
      {
         assertConnector(reader, raaFile.getName(), Connector.Version.V_16, Main.ConnectorType.BIDIRECTIONAL,
               TransactionSupportEnum.NoTransaction, false, true, false);
         assertStructure(reader, "META-INF/", "META-INF/ra.xml", "test.jar");

         // resource adapter
         assertResourceAdapter(reader, TestResourceAdapter.class);

         // managed connection factory
         assertManagedConnectionFactory(reader, TestManagedConnectionFactory.class, true, true, false, "Unknown",
               "Unknown", false, TestConnectionFactory.class, new String[] {
                  "TestConnection getConnection() throws ResourceException", "void setReference(Reference)",
                  "Reference getReference() throws NamingException"},
               TestConnection.class, new String[] {"WorkManager getWorkManager()",
                  "BootstrapContext getBootstrapContext()", "int getCreateFailureCount()",
                  "String getWorkManagerName()", "int getInvalidConnectionFailureCount()", "void close()"},
               new String[]{"CreateFailureCount (java.lang.Integer)",
                  "InvalidConnectionFailureCount (java.lang.Integer)"}, new String[0]);

         // admin object
         assertAdminObject(reader, TestAdminObjectImpl.class, TestAdminObject.class, true);

         // activation spec
         assertActivationSpec(reader, TestActivationSpec.class, TestMessageListener.class, "Name");

         // Contents of META-INF/ra.xml
         assertRAXml(reader, raArchive);

         // deployment descriptor
         assertDeploymentDescriptor(reader);
         Activations activations = parseDeploymentDescriptor(reader);
         assertNotNull(activations);
         for (Activation activation : activations.getActivations())
         {
            assertEquals(raaFile.getName(), activation.getArchive());
            assertNull(activation.getConfigProperties());
            assertEquals(TransactionSupportEnum.NoTransaction, activation.getTransactionSupport());

            final List<ConnectionDefinition> connectionDefinitions = activation.getConnectionDefinitions();
            assertEquals(1, connectionDefinitions.size());
            final ConnectionDefinition connectionDefinition = connectionDefinitions.get(0);
            assertEquals(TestManagedConnectionFactory.class.getName(), connectionDefinition.getClassName());
            assertEquals("TestConnection", connectionDefinition.getId());
            assertEquals("java:jboss/eis/TestConnection", connectionDefinition.getJndiName());
            assertNull(connectionDefinition.getRecovery());

            final Map<String, String> configProperties = connectionDefinition.getConfigProperties();
            assertNotNull(configProperties);
            assertEquals(2, configProperties.size());
            assertTrue(configProperties.containsKey("CreateFailureCount"));
            assertEquals("0", configProperties.get("CreateFailureCount"));
            assertTrue(configProperties.containsKey("InvalidConnectionFailureCount"));
            assertEquals("0", configProperties.get("InvalidConnectionFailureCount"));
            assertNotNull(connectionDefinition.getPool());
            assertNotNull(connectionDefinition.getSecurity().getSecurityDomain());

            final List<AdminObject> adminObjects = activation.getAdminObjects();
            assertEquals(1, adminObjects.size());
            final AdminObject adminObject = adminObjects.get(0);
            assertEquals(TestAdminObjectImpl.class.getName(), adminObject.getClassName());
            assertEquals("TestAdminObjectImpl", adminObject.getId());
            assertEquals("java:jboss/eis/ao/TestAdminObjectImpl", adminObject.getJndiName());
         }
      }
   }

   /**
    * Tests rarInfo tool with tx log rar.
    *
    * @throws Exception in case of an error
    */
   @Test
   public void txLogRar() throws Exception
   {
      ResourceAdapterArchive raArchive = ResourceAdapterFactory.createTxLogRar();
      raArchive.as(ZipExporter.class).exportTo(raaFile, true);
      Main.rarInfo(raaFile.getAbsolutePath(), null, false, "txlograr-output.txt");
      final File file = new File("txlograr-output.txt");
      assertTrue(file.exists());
      file.deleteOnExit();
      try (BufferedReader reader = new BufferedReader(new FileReader(file)))
      {
         assertConnector(reader, raaFile.getName(), Connector.Version.V_15, Main.ConnectorType.OUTBOUND,
               TransactionSupportEnum.XATransaction, false, true, false);
         assertStructure(reader, "META-INF/", "META-INF/ra.xml", "txlog.jar");

         // managed connection factory
         assertManagedConnectionFactory(reader, TxLogManagedConnectionFactory.class, false, false, false, "Unknown",
               "Unknown", false, TxLogConnectionFactory.class, new String[] {
                  "TxLogConnection getConnection() throws ResourceException", "void setReference(Reference)",
                  "Reference getReference() throws NamingException"},
               TxLogConnection.class, new String[] {"void fail()", "int getTransactionTimeout()",
                  "boolean setTransactionTimeout(int)", "boolean isRecovery()", "boolean isInPool()",
                  "void clearState(String)", "void clearState()", "String getId()", "String getState(String)",
                  "String getState()", "void close()"}, new String[0], new String[0]);

         // Contents of META-INF/ra.xml
         assertRAXml(reader, raArchive);

         // deployment descriptor
         assertDeploymentDescriptor(reader);
         Activations activations = parseDeploymentDescriptor(reader);
         assertNotNull(activations);
         for (Activation activation : activations.getActivations())
         {
            assertEquals(raaFile.getName(), activation.getArchive());
            assertNull(activation.getConfigProperties());
            assertEquals(TransactionSupportEnum.XATransaction, activation.getTransactionSupport());

            final List<ConnectionDefinition> connectionDefinitions = activation.getConnectionDefinitions();
            assertEquals(1, connectionDefinitions.size());
            final ConnectionDefinition connectionDefinition = connectionDefinitions.get(0);
            assertEquals(TxLogManagedConnectionFactory.class.getName(), connectionDefinition.getClassName());
            assertEquals("TxLogConnection", connectionDefinition.getId());
            assertEquals("java:jboss/eis/TxLogConnection", connectionDefinition.getJndiName());
            assertNotNull(connectionDefinition.getPool());
            assertNotNull(connectionDefinition.getSecurity().getSecurityDomain());
            final Recovery recovery = connectionDefinition.getRecovery();
            assertNotNull(recovery);
            assertEquals("domain", recovery.getCredential().getSecurityDomain());
         }
      }
   }

   /**
    * Tests rarInfo tool with unified-security rar.
    *
    * @throws Exception in case of an error
    */
   @Test
   public void unifiedSecurityRar() throws Exception
   {
      ResourceAdapterArchive raArchive = ResourceAdapterFactory.createUnifiedSecurityRar();
      raArchive.as(ZipExporter.class).exportTo(raaFile, true);
      Main.rarInfo(raaFile.getAbsolutePath(), null, false, "unified-security-output.txt");
      final File file = new File("unified-security-output.txt");
      assertTrue(file.exists());
      file.deleteOnExit();
      try (BufferedReader reader = new BufferedReader(new FileReader(file)))
      {
         assertConnector(reader, raaFile.getName(), Connector.Version.V_16, Main.ConnectorType.OUTBOUND,
               TransactionSupportEnum.XATransaction, false, true, false);
         assertStructure(reader, "META-INF/", "META-INF/ra.xml", "unified-security.jar");

         // resource adapter
         assertResourceAdapter(reader, UnifiedSecurityResourceAdapter.class);

         // managed connection factory
         assertManagedConnectionFactory(reader, UnifiedSecurityManagedConnectionFactory.class, true, true, false,
               "Unknown", "Unknown", false, UnifiedSecurityConnectionFactory.class, new String[] {
                  "UnifiedSecurityConnection getConnection() throws ResourceException",
                  "UnifiedSecurityConnection getConnection(String, String) throws ResourceException",
                  "void setReference(Reference)", "Reference getReference() throws NamingException"},
               UnifiedSecurityConnection.class, new String[] {"String getUserName()", "String getPassword()",
                  "void fail()", "int getListenerIdentity()", "void close()"}, new String[0], new String[0]);

         // Contents of META-INF/ra.xml
         assertRAXml(reader, raArchive);

         // deployment descriptor
         assertDeploymentDescriptor(reader);
         Activations activations = parseDeploymentDescriptor(reader);
         assertNotNull(activations);
         for (Activation activation : activations.getActivations())
         {
            assertEquals(raaFile.getName(), activation.getArchive());
            assertNull(activation.getConfigProperties());
            assertEquals(TransactionSupportEnum.XATransaction, activation.getTransactionSupport());

            final List<ConnectionDefinition> connectionDefinitions = activation.getConnectionDefinitions();
            assertEquals(1, connectionDefinitions.size());
            final ConnectionDefinition connectionDefinition = connectionDefinitions.get(0);
            assertEquals(UnifiedSecurityManagedConnectionFactory.class.getName(), connectionDefinition.getClassName());
            assertEquals("UnifiedSecurityConnection", connectionDefinition.getId());
            assertEquals("java:jboss/eis/UnifiedSecurityConnection", connectionDefinition.getJndiName());
            assertNotNull(connectionDefinition.getPool());
            assertNotNull(connectionDefinition.getSecurity().getSecurityDomain());
            final Recovery recovery = connectionDefinition.getRecovery();
            assertNotNull(recovery);
            assertEquals("domain", recovery.getCredential().getSecurityDomain());
         }
      }
   }

   /**
    * Tests rarInfo tool with work rar.
    *
    * @throws Exception in case of an error
    */
   @Test
   public void workRar() throws Exception
   {
      ResourceAdapterArchive raArchive = ResourceAdapterFactory.createWorkRar();
      raArchive.as(ZipExporter.class).exportTo(raaFile, true);
      Main.rarInfo(raaFile.getAbsolutePath(), null, false, "work-output.txt");
      final File file = new File("work-output.txt");
      assertTrue(file.exists());
      file.deleteOnExit();
      try (BufferedReader reader = new BufferedReader(new FileReader(file)))
      {
         assertConnector(reader, raaFile.getName(), Connector.Version.V_16, Main.ConnectorType.OUTBOUND,
               TransactionSupportEnum.NoTransaction, false, true, false);
         assertStructure(reader, "META-INF/", "META-INF/ra.xml", "work.jar");

         // resource adapter
         assertResourceAdapter(reader, WorkResourceAdapter.class);

         // managed connection factory
         assertManagedConnectionFactory(reader, WorkManagedConnectionFactory.class, false, true, false,
               "Unknown", "Unknown", false, WorkConnectionFactory.class, new String[] {
                  "WorkConnection getConnection() throws ResourceException", "void setReference(Reference)",
                  "Reference getReference() throws NamingException"},
               WorkConnection.class, new String[] {"WorkManager getWorkManager()",
                  "void scheduleWork(Work, long, ExecutionContext, WorkListener) throws WorkException",
                  "void scheduleWork(Work) throws WorkException",
                  "void doWork(Work, long, ExecutionContext, WorkListener) throws WorkException",
                  "void doWork(Work) throws WorkException",
                  "long startWork(Work, long, ExecutionContext, WorkListener) throws WorkException",
                  "long startWork(Work) throws WorkException", "void close()"}, new String[0], new String[0]);

         // Contents of META-INF/ra.xml
         assertRAXml(reader, raArchive);

         // deployment descriptor
         assertDeploymentDescriptor(reader);
         Activations activations = parseDeploymentDescriptor(reader);
         assertNotNull(activations);
         for (Activation activation : activations.getActivations())
         {
            assertEquals(raaFile.getName(), activation.getArchive());
            assertNull(activation.getConfigProperties());
            assertEquals(TransactionSupportEnum.NoTransaction, activation.getTransactionSupport());

            final List<ConnectionDefinition> connectionDefinitions = activation.getConnectionDefinitions();
            assertEquals(1, connectionDefinitions.size());
            final ConnectionDefinition connectionDefinition = connectionDefinitions.get(0);
            assertEquals(WorkManagedConnectionFactory.class.getName(), connectionDefinition.getClassName());
            assertEquals("WorkConnection", connectionDefinition.getId());
            assertEquals("java:jboss/eis/WorkConnection", connectionDefinition.getJndiName());
            assertNotNull(connectionDefinition.getPool());
            assertNotNull(connectionDefinition.getSecurity().getSecurityDomain());
            assertNull(connectionDefinition.getRecovery());
         }
      }
   }

   private void assertConnector(BufferedReader reader, String fileName, Connector.Version version,
         Main.ConnectorType connectorType, TransactionSupportEnum transactionSupportEnum, boolean reauth,
         boolean compliant, boolean nativeConn) throws IOException
   {
      String line = reader.readLine();
      assertTrue(line, line.matches("Archive:\\s*" + fileName));
      line = reader.readLine();
      assertTrue(line, line.matches("JCA version:\\s*" + version));
      line = reader.readLine();
      assertTrue(line, line.matches("Type:\\s*" + connectorType));
      line = reader.readLine();
      assertTrue(line, line.matches("Transaction:\\s*" + transactionSupportEnum));
      line = reader.readLine();
      assertTrue(line, line.matches("Reauth:\\s*" + getBooleanDescription(reauth)));
      line = reader.readLine();
      assertTrue(line, line.matches("Compliant:\\s*" + getBooleanDescription(compliant)));
      line = reader.readLine();
      assertTrue(line, line.matches("Native:\\s*" + getBooleanDescription(nativeConn)));
   }

   private void assertStructure(BufferedReader reader, String ... structureItems) throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("Structure:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
      for (String structureItem : structureItems)
      {
         line = reader.readLine();
         assertTrue(line, line.matches("\\s*" + structureItem));
      }
   }

   private void assertResourceAdapter(BufferedReader reader, Class resourceAdapterClass, String[]... configProperties)
         throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("Resource-adapter:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Class:\\s*" + resourceAdapterClass.getName()));
      for (String[] configProperty : configProperties)
      {
         line = reader.readLine().trim();
         assertTrue(line, line.matches("Config-property:\\s*" + configProperty[0] + "\\s*\\S" + configProperty[1] +
               "\\S"));
      }
   }

   private void assertManagedConnectionFactory(BufferedReader reader, Class managedConnectionFactoryClass,
         boolean validating, boolean association, boolean transactionSupport, String sharable, String enlistment,
         boolean cci, Class connectionFactoryClass, String[] connFactoryMethods, Class connectionClass,
         String[] connMethods, String[] configProperties, String[] introspectedConfigProperties) throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("Managed-connection-factory:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Class:\\s*" + managedConnectionFactoryClass.getName()));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Validating:\\s*" + getBooleanDescription(validating)));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Association:\\s*" + getBooleanDescription(association)));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("TransactionSupport:\\s*" + getBooleanDescription(transactionSupport)));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Sharable:\\s*" + sharable));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Enlistment:\\s*" + enlistment));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("CCI:\\s*" + getBooleanDescription(cci)));
      line = reader.readLine();

      // connection factory
      assertTrue(line, line.matches("\\s*ConnectionFactory\\s*\\S" + connectionFactoryClass.getName() + "\\S"));
      final HashSet<String> connFactoryMethodSet = new HashSet<>();
      for (int i = 0; i < connFactoryMethods.length; i++)
      {
         connFactoryMethodSet.add(reader.readLine().trim());
      }
      for (String connFactoryMethod: connFactoryMethods)
      {
         assertTrue("Could not find expected method: " + connFactoryMethod,
               connFactoryMethodSet.contains(connFactoryMethod));
      }

      // connection
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Connection\\s*\\S" + connectionClass.getName() + "\\S"));
      final HashSet<String> connMethodSet = new HashSet<>();
      for (int i = 0; i < connMethods.length; i++)
      {
         connMethodSet.add(reader.readLine().trim());
      }
      for (String connMethod: connMethods)
      {
         assertTrue("Could not find expected method: " + connMethod, connMethodSet.contains(connMethod));
      }

      // config properties
      for (String configProperty : configProperties)
      {
         line = reader.readLine().trim();
         assertTrue(line, line.matches("Config\\-property\\S\\s*" + configProperty.replaceAll("\\(", "\\\\(").
               replaceAll("\\)", "\\\\)").replaceAll("\\s", "\\\\s").replaceAll("\\.", "\\\\.")));
      }

      // introspected config properties
      for (String introspectedConfigProperty : introspectedConfigProperties)
      {
         line = reader.readLine().trim();
         assertTrue(line, line.matches("Introspected\\sConfig\\-property\\S\\s*" +
                     introspectedConfigProperty.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").
                           replaceAll("\\s", "\\\\s").replaceAll("\\.", "\\\\.")));
      }
   }

   private void assertAdminObject(BufferedReader reader, Class adminObjectClass, Class adminObjectInterface,
         boolean association) throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("Admin-object:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Class:\\s*" + adminObjectClass.getName()));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Association:\\s*" + getBooleanDescription(association)));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Interface:\\s*" + adminObjectInterface.getName()));
   }

   private void assertActivationSpec(BufferedReader reader, Class activationSpecClass, Class messageListenerClass,
         String... requiredConfigProperties) throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("Activation-spec:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Class:\\s*" + activationSpecClass.getName()));
      line = reader.readLine().trim();
      assertTrue(line, line.matches("Message\\-listener:\\s*" + messageListenerClass.getName()));
      for (String requiredConfigProperty : requiredConfigProperties)
      {
         line = reader.readLine().trim();
         assertTrue(line, line.matches("Required\\-config\\-property:\\s*" + requiredConfigProperty));
      }
   }


   private void assertRAXml(BufferedReader reader, ResourceAdapterArchive raArchive) throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("META-INF/ra.xml:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
      BufferedReader raReader = new BufferedReader(new InputStreamReader(raArchive.get("META-INF/ra.xml").getAsset().
            openStream()));
      while ((line = raReader.readLine()) != null)
      {
         assertEquals(line, reader.readLine());
      }
   }

   private void assertDeploymentDescriptor(BufferedReader reader) throws IOException
   {
      String line = readNextNonemptyLine(reader);
      assertEquals("Deployment descriptor:", line);
      line = reader.readLine();
      assertTrue(line, line.matches("-+"));
   }

   private Activations parseDeploymentDescriptor(BufferedReader reader) throws Exception
   {
      final XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(reader);
      final ResourceAdapterParser parser = new ResourceAdapterParser();
      return parser.parse(xsr);
   }

   private String readNextNonemptyLine(BufferedReader reader) throws IOException
   {
      String line;
      do
      {
         line = reader.readLine().trim();
      } while (line.isEmpty());
      return line;
   }

   private String getBooleanDescription(boolean value)
   {
      return value ? "Yes" : "No";
   }
}
