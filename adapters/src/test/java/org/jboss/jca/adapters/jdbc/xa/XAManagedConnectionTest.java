/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.xa;

import org.jboss.jca.adapters.AdaptersLogger;
import org.jboss.jca.adapters.jdbc.BaseWrapperManagedConnectionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test case for XAManagedConnection, specifically testing error broadcasting
 * behavior in start() and end() methods.
 */
public class XAManagedConnectionTest {

    private XAManagedConnectionFactory mcf;
    private XAConnection xaConnection;
    private XAResource xaResource;
    private Connection connection;
    private XAManagedConnection xaManagedConnection;
    private Xid testXid;
    private AdaptersLogger logger;

    @Before
    public void setup() throws Exception {
        // Create mocks
        xaConnection = mock(XAConnection.class);
        xaResource = mock(XAResource.class);
        connection = mock(Connection.class);
        logger = mock(AdaptersLogger.class);

        // Setup logger behavior
        when(logger.isTraceEnabled()).thenReturn(false);

        // Setup XAConnection to return the mocked XAResource and Connection
        when(xaConnection.getXAResource()).thenReturn(xaResource);
        when(xaConnection.getConnection()).thenReturn(connection);

        // Setup Connection mock defaults
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_COMMITTED);
        when(connection.isReadOnly()).thenReturn(false);

        // Create test Xid
        testXid = new TestXid(1, new byte[]{1}, new byte[]{1});

        // Create a test MCF instance with mocked logger
        mcf = new TestXAManagedConnectionFactory(logger);

        // Create the XAManagedConnection instance
        xaManagedConnection = new XAManagedConnection(mcf, xaConnection, new Properties(), -1, 0);
    }

    @Test
    public void testStartMethodBroadcastsErrorOnFatalXAException() throws Exception {
        // Setup: XAResource.start() throws a fatal XAException (not a rollback error)
        XAException fatalException = new XAException("Fatal XA error");
        fatalException.errorCode = XAException.XAER_RMFAIL; // Fatal error code
        doThrow(fatalException).when(xaResource).start(any(Xid.class), anyInt());

        // Add a connection event listener to verify error broadcasting
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call start and expect XAException to be thrown
        try {
            xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XAER_RMFAIL, e.errorCode);
        }

        // Verify: Error was broadcast to listeners
        Assert.assertNotNull("Connection error event should have been fired", listener.errorEvent);
        Assert.assertEquals(ConnectionEvent.CONNECTION_ERROR_OCCURRED, listener.errorEvent.getId());
        Assert.assertSame(fatalException, listener.errorEvent.getException());
    }

    @Test
    public void testStartMethodDoesNotBroadcastErrorOnRollbackXAException() throws Exception {
        // Setup: XAResource.start() throws a rollback XAException (XA_RB* errors)
        XAException rollbackException = new XAException("Rollback error");
        rollbackException.errorCode = XAException.XA_RBROLLBACK; // Rollback error code
        doThrow(rollbackException).when(xaResource).start(any(Xid.class), anyInt());

        // Add a connection event listener
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call start and expect XAException to be thrown
        try {
            xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
        }

        // Verify: Error was NOT broadcast (rollback errors are not fatal)
        Assert.assertNull("Connection error event should NOT have been fired for rollback errors",
                          listener.errorEvent);
    }

    @Test
    public void testEndMethodBroadcastsErrorOnFatalXAException() throws Exception {
        // Setup: First successfully start the transaction
        doNothing().when(xaResource).start(any(Xid.class), anyInt());
        xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);

        // Setup: XAResource.end() throws a fatal XAException
        XAException fatalException = new XAException("Fatal XA error on end");
        fatalException.errorCode = XAException.XAER_RMFAIL;
        doThrow(fatalException).when(xaResource).end(any(Xid.class), anyInt());

        // Add a connection event listener
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call end and expect XAException to be thrown
        try {
            xaManagedConnection.end(testXid, XAResource.TMSUCCESS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XAER_RMFAIL, e.errorCode);
        }

        // Verify: Error was broadcast to listeners
        Assert.assertNotNull("Connection error event should have been fired", listener.errorEvent);
        Assert.assertEquals(ConnectionEvent.CONNECTION_ERROR_OCCURRED, listener.errorEvent.getId());
        Assert.assertSame(fatalException, listener.errorEvent.getException());
    }

    @Test
    public void testEndMethodDoesNotBroadcastErrorOnRollbackXAException() throws Exception {
        // Setup: First successfully start the transaction
        doNothing().when(xaResource).start(any(Xid.class), anyInt());
        xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);

        // Setup: XAResource.end() throws a rollback XAException
        XAException rollbackException = new XAException("Rollback error on end");
        rollbackException.errorCode = XAException.XA_RBROLLBACK;
        doThrow(rollbackException).when(xaResource).end(any(Xid.class), anyInt());

        // Add a connection event listener
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call end and expect XAException to be thrown
        try {
            xaManagedConnection.end(testXid, XAResource.TMSUCCESS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XA_RBROLLBACK, e.errorCode);
        }

        // Verify: Error was NOT broadcast
        Assert.assertNull("Connection error event should NOT have been fired for rollback errors",
                          listener.errorEvent);
    }

    @Test
    public void testEndMethodPreventsDuplicateBroadcastForSameXid() throws Exception {
        // Setup: First successfully start the transaction
        doNothing().when(xaResource).start(any(Xid.class), anyInt());
        xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);

        // Setup: XAResource.end() throws a fatal XAException
        XAException fatalException = new XAException("Fatal XA error");
        fatalException.errorCode = XAException.XAER_RMFAIL;
        doThrow(fatalException).when(xaResource).end(any(Xid.class), anyInt());

        // Add a connection event listener
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call end twice with same Xid (both should fail)
        try {
            xaManagedConnection.end(testXid, XAResource.TMSUCCESS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
        }

        // First call should have broadcast the error
        Assert.assertNotNull("First call should have broadcast error", listener.errorEvent);
        listener.errorEvent = null; // Reset

        // Second call with same Xid
        try {
            xaManagedConnection.end(testXid, XAResource.TMSUCCESS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
        }

        // Verify: Error was NOT broadcast again for the same Xid
        Assert.assertNull("Second call should NOT have broadcast error for same Xid",
                          listener.errorEvent);
    }
    @Test
    public void testStartMethodThrowsBroadcastForSameXid() throws Exception {
        // Setup: XAResource.start() throws a fatal XAException (not a rollback error)
        XAException fatalException = new XAException("Fatal XA error");
        fatalException.errorCode = XAException.XAER_RMFAIL; // Fatal error code
        doThrow(fatalException).when(xaResource).start(any(Xid.class), anyInt());

        // Add a connection event listener to verify error broadcasting
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call start and expect XAException to be thrown
        try {
            xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XAER_RMFAIL, e.errorCode);
        }

        // Verify: Error was broadcast to listeners
        Assert.assertNotNull("Connection error event should have been fired", listener.errorEvent);
        Assert.assertEquals(ConnectionEvent.CONNECTION_ERROR_OCCURRED, listener.errorEvent.getId());
        Assert.assertSame(fatalException, listener.errorEvent.getException());

        // First call should have broadcast the error
        Assert.assertNotNull("First call should have broadcast error", listener.errorEvent);
        listener.errorEvent = null; // Reset

        // Second call with same Xid
        try {
            xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XAER_RMFAIL, e.errorCode);
        }

        // Verify: Error was broadcast again for the same Xid
        Assert.assertNotNull("Second call should have broadcast error for same Xid",
                          listener.errorEvent);
        Assert.assertEquals(ConnectionEvent.CONNECTION_ERROR_OCCURRED, listener.errorEvent.getId());
        Assert.assertSame(fatalException, listener.errorEvent.getException());
    }
    @Test
    public void testStartMethodThrowsBroadcastForDifferentXid() throws Exception {
        // Setup: XAResource.start() throws a fatal XAException (not a rollback error)
        XAException fatalException = new XAException("Fatal XA error");
        fatalException.errorCode = XAException.XAER_RMFAIL; // Fatal error code
        doThrow(fatalException).when(xaResource).start(any(Xid.class), anyInt());

        // Add a connection event listener to verify error broadcasting
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Call start and expect XAException to be thrown
        try {
            xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XAER_RMFAIL, e.errorCode);
        }

        // Verify: Error was broadcast to listeners
        Assert.assertNotNull("Connection error event should have been fired", listener.errorEvent);
        Assert.assertEquals(ConnectionEvent.CONNECTION_ERROR_OCCURRED, listener.errorEvent.getId());
        Assert.assertSame(fatalException, listener.errorEvent.getException());

        // First call should have broadcast the error
        Assert.assertNotNull("First call should have broadcast error", listener.errorEvent);
        listener.errorEvent = null; // Reset

        // Second call with different Xid
        try {
            xaManagedConnection.start(new TestXid(1, new byte[]{1}, new byte[]{1}), XAResource.TMNOFLAGS);
            Assert.fail("Expected XAException to be thrown");
        } catch (XAException e) {
            // Expected
            Assert.assertEquals(XAException.XAER_RMFAIL, e.errorCode);
        }

        // Verify: Error was broadcast again for a different Xid
        Assert.assertNotNull("Second call should have broadcast error for a different Xid",
                          listener.errorEvent);
        Assert.assertEquals(ConnectionEvent.CONNECTION_ERROR_OCCURRED, listener.errorEvent.getId());
        Assert.assertSame(fatalException, listener.errorEvent.getException());
    }

    @Test
    public void testSuccessfulStartAndEnd() throws Exception {
        // Setup: XAResource operations succeed
        doNothing().when(xaResource).start(any(Xid.class), anyInt());
        doNothing().when(xaResource).end(any(Xid.class), anyInt());

        // Add a connection event listener
        TestConnectionEventListener listener = new TestConnectionEventListener();
        xaManagedConnection.addConnectionEventListener(listener);

        // Execute: Start transaction
        xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);

        // Verify: start was called on underlying XAResource
        verify(xaResource).start(eq(testXid), eq(XAResource.TMNOFLAGS));

        // Verify: No error was broadcast
        Assert.assertNull("No error should have been broadcast on successful start",
                          listener.errorEvent);

        // Execute: End transaction
        xaManagedConnection.end(testXid, XAResource.TMSUCCESS);

        // Verify: end was called on underlying XAResource
        verify(xaResource).end(eq(testXid), eq(XAResource.TMSUCCESS));

        // Verify: Still no error was broadcast
        Assert.assertNull("No error should have been broadcast on successful end",
                          listener.errorEvent);
    }

    @Test
    public void testStartWithDifferentErrorCodes() throws Exception {
        // Test various fatal error codes
        int[] fatalErrorCodes = {
            XAException.XAER_RMFAIL,
            XAException.XAER_NOTA,
            XAException.XAER_INVAL,
            XAException.XAER_PROTO,
            XAException.XAER_RMERR,
            XAException.XAER_DUPID
        };

        for (int errorCode : fatalErrorCodes) {
            // Reset the managed connection for each test
            xaManagedConnection = new XAManagedConnection(mcf, xaConnection, new Properties(), -1, 0);

            XAException exception = new XAException("Test error");
            exception.errorCode = errorCode;
            doThrow(exception).when(xaResource).start(any(Xid.class), anyInt());

            TestConnectionEventListener listener = new TestConnectionEventListener();
            xaManagedConnection.addConnectionEventListener(listener);

            try {
                xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);
                Assert.fail("Expected XAException with code " + errorCode);
            } catch (XAException e) {
                // Expected
            }

            Assert.assertNotNull("Error code " + errorCode + " should trigger broadcast",
                                 listener.errorEvent);
        }
    }

    @Test
    public void testEndWithDifferentRollbackErrorCodes() throws Exception {
        // Test various rollback error codes that should NOT trigger broadcast
        int[] rollbackErrorCodes = {
            XAException.XA_RBROLLBACK,
            XAException.XA_RBCOMMFAIL,
            XAException.XA_RBDEADLOCK,
            XAException.XA_RBINTEGRITY,
            XAException.XA_RBOTHER,
            XAException.XA_RBPROTO,
            XAException.XA_RBTIMEOUT,
            XAException.XA_RBTRANSIENT
        };

        for (int errorCode : rollbackErrorCodes) {
            // Reset and start transaction
            xaManagedConnection = new XAManagedConnection(mcf, xaConnection, new Properties(), -1, 0);
            doNothing().when(xaResource).start(any(Xid.class), anyInt());
            xaManagedConnection.start(testXid, XAResource.TMNOFLAGS);

            XAException exception = new XAException("Test rollback error");
            exception.errorCode = errorCode;
            doThrow(exception).when(xaResource).end(any(Xid.class), anyInt());

            TestConnectionEventListener listener = new TestConnectionEventListener();
            xaManagedConnection.addConnectionEventListener(listener);

            try {
                xaManagedConnection.end(testXid, XAResource.TMSUCCESS);
                Assert.fail("Expected XAException with code " + errorCode);
            } catch (XAException e) {
                // Expected
            }

            Assert.assertNull("Rollback error code " + errorCode + " should NOT trigger broadcast",
                              listener.errorEvent);
        }
    }

    /**
     * Test MCF that allows setting a custom logger via reflection
     */
    private static class TestXAManagedConnectionFactory extends XAManagedConnectionFactory {
        public TestXAManagedConnectionFactory(AdaptersLogger logger) throws Exception {
            super();
            // Use reflection to set the log field from BaseWrapperManagedConnectionFactory
            java.lang.reflect.Field logField = BaseWrapperManagedConnectionFactory.class.getDeclaredField("log");
            logField.setAccessible(true);
            logField.set(this, logger);
        }
    }

    /**
     * Test implementation of ConnectionEventListener for capturing events
     */
    private static class TestConnectionEventListener implements ConnectionEventListener {
        public ConnectionEvent errorEvent;

        @Override
        public void connectionClosed(ConnectionEvent event) {
            // Not used in these tests
        }

        @Override
        public void localTransactionStarted(ConnectionEvent event) {
            // Not used in these tests
        }

        @Override
        public void localTransactionCommitted(ConnectionEvent event) {
            // Not used in these tests
        }

        @Override
        public void localTransactionRolledback(ConnectionEvent event) {
            // Not used in these tests
        }

        @Override
        public void connectionErrorOccurred(ConnectionEvent event) {
            this.errorEvent = event;
        }
    }

    /**
     * Simple Xid implementation for testing
     */
    private static class TestXid implements Xid {
        private final int formatId;
        private final byte[] globalTransactionId;
        private final byte[] branchQualifier;

        public TestXid(int formatId, byte[] globalTransactionId, byte[] branchQualifier) {
            this.formatId = formatId;
            this.globalTransactionId = globalTransactionId;
            this.branchQualifier = branchQualifier;
        }

        @Override
        public int getFormatId() {
            return formatId;
        }

        @Override
        public byte[] getGlobalTransactionId() {
            return globalTransactionId;
        }

        @Override
        public byte[] getBranchQualifier() {
            return branchQualifier;
        }
    }
}
