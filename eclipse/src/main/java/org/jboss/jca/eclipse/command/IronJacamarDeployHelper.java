/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.eclipse.command;

import org.jboss.jca.eclipse.Activator;
import org.jboss.jca.eclipse.preferences.PreferenceConstants;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * The deploy helper used to deploy rar to IronJacamar.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 */
public class IronJacamarDeployHelper
{

   /**
    * The constructor.
    */
   public IronJacamarDeployHelper()
   {
      super();
   }
   
   /**
    * Deploy file to IronJacamar server.
    * 
    * @param file The file to be deployed
    * @param monitor the IProgressMonitor
    * @return true if the deployment succeeds, false otherwise
    * @throws UnknownHostException on unknown host
    * @throws IOException on any I/O exception
    * @throws ClassNotFoundException on any class path exception
    * @throws Throwable on failure of deployment
    */
   public boolean deploy(IFile file, IProgressMonitor monitor) throws UnknownHostException,
   IOException, ClassNotFoundException, Throwable
   {
      String msg = "Deploy " + file.getName() + " to IronJacamar server.";
      monitor.subTask(msg);
      Socket socket = null;
      ObjectOutputStream output = null;
      ObjectInputStream input = null;
      try
      {
         socket = connectToServer();
         output = new ObjectOutputStream(socket.getOutputStream());
         sendCommand(output, file);
         output.flush();
         
         input = new ObjectInputStream(socket.getInputStream());
         Serializable result = (Serializable)input.readObject();
         if (result instanceof Throwable)
         {
            throw (Throwable)result;
         }
         else if (result instanceof Boolean)
         {
            return (Boolean)result;
         }
         throw new IllegalStateException("Wrong type of return value: " + result);
      }
      finally
      {
         close(input);
         close(output);
         if (socket != null)
         {
            socket.close();
         }
      }
   }
   
   /**
    * Sends deploy command to IronJacamar server.
    * 
    * @param output the OutputStream retrieved from connected Socket.
    * @param file the RAR file which needs to be deployed.
    * @throws IOException on any I/O exception
    */
   private void sendCommand(ObjectOutputStream output, IFile file) throws IOException
   {
      if (isLocalIronJacamar())
      {
         output.writeUTF("local-deploy");
         output.writeInt(1);
         output.writeObject(file.getLocationURI().toURL());
      }
      else
      {
         output.writeUTF("remote-deploy");
         output.writeInt(2);
         output.writeObject(file.getName());
         byte[] fileContent = readRarFileContent(file);
         output.writeObject(fileContent);
      }
      
   }
   
   /**
    * Checks whether current IronJacamar is located in local machine or remote machine.
    * 
    * @return true if current IronJacamar is located at local machine, false otherwise.
    * @throws UnknownHostException on unknown host
    */
   private boolean isLocalIronJacamar() throws UnknownHostException
   {
      String ijHost = getRemoteIronJacamarHost();
      InetAddress ijAddr = InetAddress.getByName(ijHost);
      if (ijAddr.isLoopbackAddress())
      {
         return true;
      }
      String localHostName = InetAddress.getLocalHost().getHostName();
      if (localHostName.equals(ijHost))
      {
         return true;
      }
      String ijIP = ijAddr.getHostAddress();
      InetAddress localIPs[] = InetAddress.getAllByName(localHostName);
      for (InetAddress localIP: localIPs)
      {
         if (ijIP.equals(localIP.getHostAddress()))
         {
            return true;
         }
      }
      
      return false;
   }

   /**
    * Connects to remote IronJacamar server
    * 
    * @return the Socket which represents the connection
    * @throws UnknownHostException on unknown host
    * @throws IOException on any I/O exception
    */
   private Socket connectToServer() throws UnknownHostException, IOException
   {
      String host = getRemoteIronJacamarHost();
      int port = getRemoteIronJacamarPort();
      return new Socket(host, port);
   }
   
   /**
    * Closes the Closeable.
    * 
    * @param closeable the Closeable
    * @throws IOException on any I/O exception
    */
   private void close(Closeable closeable) throws IOException
   {
      if (null != closeable)
      {
         closeable.close();
      }
   }

   /**
    * Reads file content to a byte array.
    *  
    * @param ifile the file
    * @return byte array
    * @throws IOException on any I/O exception
    */
   private byte[] readRarFileContent(IFile ifile) throws IOException
   {
      File file = ifile.getLocation().toFile();
      InputStream in = null;
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try
      {
         in = new FileInputStream(file);
         byte[] buf = new byte[2048];
         int readLength = 0;
         while ((readLength = in.read(buf, 0, buf.length)) > -1)
         {
            output.write(buf, 0, readLength);
         }
         return output.toByteArray();
      }
      finally
      {
         close(in);
         close(output);
      }
   }
   
   /**
    * Gets host of remote IronJacamar.
    * 
    * @return host of remote IronJacamar or empty string if not set
    */
   public String getRemoteIronJacamarHost()
   {
      String host = getPrefenceValue(PreferenceConstants.JCA_REMOTE_HOST);
      if (host == null || host.length() == 0)
      {
         return "localhost";
      }
      return host;
   }
   
   /**
    * Gets Preference string value by the preference name.
    * 
    * @param prefName the preference name
    * @return string value of the preference
    */
   private String getPrefenceValue(String prefName)
   {
      IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
      String value = prefStore.getString(prefName);
      String deValue = prefStore.getDefaultString(prefName);
      if (value != null && value.length() > 0)
      {
         return value;
      }
      return deValue;
   }
   
   /**
    * Gets port of remote IronJacamar.
    * 
    * @return port of remote IronJacamar or {@link PreferenceConstants.DEFAULT_PORT} if not set
    */
   public int getRemoteIronJacamarPort()
   {
      IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
      int port = prefStore.getInt(PreferenceConstants.JCA_REMOTE_PORT);
      if (port == 0)
      {
         port = PreferenceConstants.DEFAULT_PORT;
      }
      return port;
   }
}
