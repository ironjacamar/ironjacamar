/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import com.github.fungal.spi.deployers.DeployException;

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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * <code>DeployHandler</code> will deploy the generated RAR file to <strong>IronJacamar</strong> server.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class DeployHandler extends AbstractIronJacamarHandler
{
   /**
    * The Constructor
    */
   public DeployHandler()
   {  
   }

   /**
    * the command has been executed, so extract the needed information
    * from the application context.
    * 
    * @param event ExecutionEvent
    * @return Object null
    * @throws ExecutionException ExecutionException
    */
   @Override
   public Object execute(ExecutionEvent event) throws ExecutionException
   {
      
      setBaseEnabled(false);
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      
      // check current selected project
      final IProject project = getSelectedProject(selection);
      if (project == null)
      {
         setBaseEnabled(true);
         throw new ExecutionException("There is no IronJacamar project selected.");
      }
      
      // lookup generated rar file
      IFile rarFile = lookupRarFile(project);
      
      // rar is not generated, build it first
      if (rarFile == null || !rarFile.exists())
      {
         try
         {
            buildRar(project);
         }
         catch (ExecutionException e)
         {
            setBaseEnabled(true);
            IStatus errStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can not build project", e.getCause());
            StatusManager.getManager().handle(errStatus, StatusManager.SHOW);
            throw e;
         }
      }
      else
      {
         DeployJob deployJob = new DeployJob(rarFile);
         deployJob.schedule();
      }
      return null;
   }
   
   /**
    * <code>RemoteDeployJob</code> will transfer the RAR file to remote <strong>IronJacamar</strong> server.
    * 
    * It assumes that the RAR has been generated already.
    */
   private class DeployJob extends Job
   {
      private final IFile rarFile;
      
      private DeployJob(IFile file)
      {
         super("Deplying " + file.getFullPath() + " to IronJacamar server");
         this.rarFile = file;
      }
      
      @Override
      protected IStatus run(final IProgressMonitor monitor)
      {
         monitor.beginTask(getName(), 1);
         String host = getRemoteIronJacamarHost();
         int port = getRemoteIronJacamarPort();
         try
         {
            if (deploy(rarFile, monitor))
            {
               String msg = "Deploy " + rarFile.getFullPath() + " to server " + host + ":" + port + 
                     " successfully!";
               logMessageToConsole(msg);
               return new Status(IStatus.INFO, Activator.PLUGIN_ID, msg);
            }
            
            String msg = "Can not deploy " + rarFile.getFullPath() + " to server " + host + ":" + port;
            logMessageToConsole(msg, new Color(null, 255, 0, 0));
            return new Status(IStatus.WARNING, Activator.PLUGIN_ID, msg);
            
         }
         catch (Throwable t)
         {
            if (t instanceof DeployException)
            {
               t = t.getCause();
            }
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
                  "Can not deploy " + rarFile.getFullPath() + " to server", t);
         }
         finally
         {
            monitor.worked(1);
            enableHandler();
         }
      }
      
      /**
       * Deploy RAR file to IronJacamar server.
       * 
       * @param rarFile The RAR file
       * @param monitor the IProgressMonitor
       * @return true if the deployment succeeds, false otherwise
       * @throws UnknownHostException on unknown host
       * @throws IOException on any I/O exception
       * @throws ClassNotFoundException on any class path exception
       * @throws Throwable on failure of deployment
       */
      private boolean deploy(IFile rarFile, IProgressMonitor monitor) throws UnknownHostException,
      IOException, ClassNotFoundException, Throwable
      {
         String msg = "Deploy " + rarFile.getName() + " to IronJacamar server.";
         monitor.subTask(msg);
         logMessageToConsole(msg);
         Socket socket = null;
         ObjectOutputStream output = null;
         ObjectInputStream input = null;
         try
         {
            socket = connectToServer();
            output = new ObjectOutputStream(socket.getOutputStream());
            sendCommand(output, rarFile);
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
       * @param rarFile the RAR file which needs to be deployed.
       * @throws IOException on any I/O exception
       */
      private void sendCommand(ObjectOutputStream output, IFile rarFile) throws IOException
      {
         if (isLocalIronJacamar())
         {
            output.writeUTF("local-deploy");
            output.writeInt(1);
            output.writeObject(rarFile.getLocationURI().toURL());
         }
         else
         {
            output.writeUTF("remote-deploy");
            output.writeInt(2);
            output.writeObject(rarFile.getName());
            byte[] fileContent = readRarFileContent(rarFile);
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
       * Reads RAR file content to a byte array.
       *  
       * @param rarFile the RAR file
       * @return byte array
       * @throws IOException on any I/O exception
       */
      private byte[] readRarFileContent(IFile rarFile) throws IOException
      {
         File file = rarFile.getLocation().toFile();
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
   }

   @Override
   protected void onBuildFinished(IProject project)
   {
      try
      {
         project.refreshLocal(IResource.DEPTH_INFINITE, null);
         IFile rarFile = lookupRarFile(project);
         if (rarFile != null && rarFile.exists())
         {
            DeployJob deployJob = new DeployJob(rarFile);
            deployJob.schedule();
         }
         else
         {
            enableHandler();
         }
      }
      catch (CoreException e)
      {
         e.printStackTrace();
      }
   }
}
