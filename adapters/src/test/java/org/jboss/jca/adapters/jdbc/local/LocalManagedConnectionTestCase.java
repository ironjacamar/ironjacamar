package org.jboss.jca.adapters.jdbc.local;

import org.jboss.jca.adapters.jdbc.WrappedConnection;
import org.jboss.jca.adapters.jdbc.local.testimpl.MockDriver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

public class LocalManagedConnectionTestCase {

    private ConnectionEvent connectionEvent;
    private LocalManagedConnectionFactory connectionFactory;
    private Subject subject;

    @Before
    public void setup() {
        connectionFactory = new LocalManagedConnectionFactory();
        connectionFactory.setDriverClass(MockDriver.class.getName());

        subject = new Subject();
        PasswordCredential pc = new PasswordCredential("test", "test".toCharArray());
        pc.setManagedConnectionFactory(connectionFactory);
        subject.getPrivateCredentials().add(pc);
    }

    @Test
    public void testConnectionEventListenersNotifiedAfterConnectionCleanup() throws Exception {
        // 1. create managed connection and add a listener
        ManagedConnection mc = connectionFactory.createManagedConnection(subject, null);
        mc.addConnectionEventListener(new ConnectionEventListenerStub());

        // 2. create connection handle
        WrappedConnection handle = (WrappedConnection) mc.getConnection(subject, null);
        Assert.assertNotNull(handle);
        Assert.assertFalse(handle.isClosed());

        // 3. cleanup managed connection
        mc.cleanup();

        // 4. listeners should have been notified that handles were closed
        Assert.assertTrue(handle.isClosed());
        Assert.assertNotNull("Connection listener not called.", connectionEvent);
        Assert.assertEquals(handle, connectionEvent.getConnectionHandle());
        Assert.assertEquals(ConnectionEvent.CONNECTION_CLOSED, connectionEvent.getId());
    }

    /**
     * Listener that would catch connection closed event.
     */
    private class ConnectionEventListenerStub implements ConnectionEventListener {
        @Override
        public void connectionClosed(ConnectionEvent event) {
            connectionEvent = event;
        }

        @Override
        public void localTransactionStarted(ConnectionEvent event) {

        }

        @Override
        public void localTransactionCommitted(ConnectionEvent event) {

        }

        @Override
        public void localTransactionRolledback(ConnectionEvent event) {

        }

        @Override
        public void connectionErrorOccurred(ConnectionEvent event) {

        }
    }
}
