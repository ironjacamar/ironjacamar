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

package org.jboss.jca.core.workmanager.unit;

import org.jboss.jca.arquillian.embedded.Configuration;
import org.jboss.jca.arquillian.embedded.Inject;
import org.jboss.jca.core.api.workmanager.WorkManager;
import org.jboss.jca.core.workmanager.rars.dwm.WorkConnection;
import org.jboss.jca.core.workmanager.rars.dwm.WorkConnectionFactory;
import org.jboss.jca.embedded.dsl.InputStreamDescriptor;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Work manager test case for security inflow
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@RunWith(Arquillian.class)
@Configuration(autoActivate = false)
public class WorkManagerSecurityTestCase
{
   private static Logger log = Logger.getLogger(WorkManagerSecurityTestCase.class);

   // --------------------------------------------------------------------------------||
   // Deployments --------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Define the work manager deployment
    * @return The deployment archive
    */
   @Deployment(name = "WMSec", order = 1)
   public static InputStreamDescriptor createDistributedWorkManagerDeployment()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("wm-security.xml",
                                                            cl.getResourceAsStream("wm-security.xml"));
      return isd;
   }

   /**
    * Define the resource adapter deployment
    * @return The deployment archive
    */
   @Deployment(name = "RAR", order = 2)
   public static ResourceAdapterArchive createArchiveDeployment()
   {
      ResourceAdapterArchive raa =
         ShrinkWrap.create(ResourceAdapterArchive.class, "work.rar");

      JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
      ja.addPackage(WorkConnectionFactory.class.getPackage());

      raa.addAsLibrary(ja);
      raa.addAsManifestResource("rars/dwm/META-INF/ra.xml", "ra.xml");

      return raa;
   }

   /**
    * Define the activation deployment
    * @return The deployment archive
    */
   @Deployment(name = "ACT", order = 3)
   public static InputStreamDescriptor createActivationDeployment()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStreamDescriptor isd = new InputStreamDescriptor("wm-security-ra.xml",
                                                            cl.getResourceAsStream("wm-security-ra.xml"));
      return isd;
   }

   @Resource(mappedName = "java:/eis/WorkConnectionFactory")
   private WorkConnectionFactory wcf;

   @Inject(name = "WorkManagerSecurity")
   private WorkManager workManagerSecurity;

   @Inject(name = "BootstrapContextSecurity")
   private BootstrapContext bootstrapContextSecurity;

   // --------------------------------------------------------------------------------||
   // Tests --------------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Test that the callback is correctly configured
    * @throws Throwable throwable exception
    */
   @Test
   public void testCallback() throws Throwable
   {
      assertNotNull(wcf);
      assertNotNull(workManagerSecurity);

      org.jboss.jca.core.spi.security.Callback defaultCallback = workManagerSecurity.getCallbackSecurity();
      assertNotNull(defaultCallback);
      assertFalse("mapping-required", defaultCallback.isMappingRequired());
      assertEquals("domain", "other", defaultCallback.getDomain());
      assertNull("default-principal", defaultCallback.getDefaultPrincipal());
      assertNull("default-groups", defaultCallback.getDefaultGroups());
      assertNull("mapping-user", defaultCallback.mapPrincipal("eis"));
      assertNull("mapping-group", defaultCallback.mapGroup("eis"));

      WorkConnection wc = wcf.getConnection();
      try
      {
         WorkManager wm = (WorkManager)wc.getWorkManager();
         assertNotNull(wm);

         org.jboss.jca.core.spi.security.Callback callback = wm.getCallbackSecurity();
         assertNotNull(callback);
         assertTrue("mapping-required", callback.isMappingRequired());
         assertEquals("domain", "other", callback.getDomain());
         assertEquals("default-principal", new SimplePrincipal("default_principal"), callback.getDefaultPrincipal());
         assertArrayEquals("default-groups", new String[] {"default_group"}, callback.getDefaultGroups());
         assertEquals("mapping-user", new SimplePrincipal("jca"), callback.mapPrincipal("eis"));
         assertEquals("mapping-group", "jca", callback.mapGroup("eis"));
      }
      finally
      {
         wc.close();
      }
   }

   /**
    * Test that a Work is executed in a security context
    * @throws Throwable throwable exception
    */
   @Ignore("SECURITY-731, SECURITY-732")
   public void testSecurityContext() throws Throwable
   {
      assertNotNull(wcf);

      WorkConnection wc = wcf.getConnection();
      try
      {
         MyWork myWork = new MyWork();
         wc.doWork(myWork);

         log.infof("Principals=%s", myWork.getPrincipals());
         
         assertNotNull("principals-not-null", myWork.getPrincipals());
         assertTrue("principals-contains-default", 
                    myWork.getPrincipals().contains(new SimplePrincipal("default_principal")));
         assertTrue("principals-contains-mapping",
                    myWork.getPrincipals().contains(new SimplePrincipal("jca")));

         assertNotNull("roles-not-null", myWork.getRoles());
         log.infof("Roles=%s", Arrays.toString(myWork.getRoles()));
         assertTrue("roles-contains-default", myWork.hasRole("default_group"));
         assertTrue("roles-contains-mapping", myWork.hasRole("jca"));
      }
      finally
      {
         wc.close();
      }
   }

   // --------------------------------------------------------------------------------||
   // Helper classes -----------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * Work
    */
   public static class MyWork implements Work, WorkContextProvider
   {
      private static final long serialVersionUID = 1L;
      private Set<Principal> principals;
      private String[] roles;

      /**
       * Constructor
       */
      public MyWork()
      {
         this.principals = null;
         this.roles = null;
      }

      /**
       * {@inheritDoc}
       */
      public List<WorkContext> getWorkContexts()
      {
         List<WorkContext> l = new ArrayList<WorkContext>(1);
         l.add(new MySecurityContext());
         return l;
      }

      /**
       * {@inheritDoc}
       */
      public void run()
      {
         log.info("MyWork: run");

         org.jboss.security.SecurityContext securityContext = SecurityContextAssociation.getSecurityContext();
         if (securityContext != null && securityContext.getSubjectInfo() != null)
         {
            log.infof("SecurityContext=%s", securityContext);

            Subject subject = securityContext.getSubjectInfo().getAuthenticatedSubject();
            if (subject != null)
            {
               log.infof("Subject=%s", subject);
               log.infof("Private credentials=%s", subject.getPrivateCredentials());
               log.infof("Public credentials=%s", subject.getPublicCredentials());

               if (subject.getPrincipals() != null && subject.getPrincipals().size() > 0)
               {
                  principals = subject.getPrincipals();
               }

               org.jboss.security.identity.RoleGroup pbRoles = securityContext.getUtil().getRoles();
               if (pbRoles != null)
               {
                  List<String> l = new ArrayList<String>(pbRoles.getRoles().size());
                  for (org.jboss.security.identity.Role role : pbRoles.getRoles())
                  {
                     l.add(role.getRoleName());
                  }
                  roles = l.toArray(new String[l.size()]);
               }
            }
         }
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
         log.info("MyWork: release");
      }

      /**
       * Get the principals
       * @return The value
       */
      public Set<Principal> getPrincipals()
      {
         return principals;
      }

      /**
       * Get the roles
       * @return The value
       */
      public String[] getRoles()
      {
         return roles;
      }

      /**
       * Has role
       * @param r The name of the role
       * @return True if the role is present; otherwise false
       */
      public boolean hasRole(String r)
      {
         if (roles != null)
         {
            for (String role : roles)
            {
               if (role.equals(r))
                  return true;
            }
         }

         return false;
      }
   }

   /**
    * SecurityContext
    */
   public static class MySecurityContext extends SecurityContext
   {
      private static final long serialVersionUID = 1L;

      /**
       * Constructor
       */
      public MySecurityContext()
      {
         super();
      }

      /**
       * {@inheritDoc}
       */
      public void setupSecurityContext(CallbackHandler handler, Subject executionSubject, Subject serviceSubject)
      {
         log.info("MySecurityContext: setupSecurityContext(" + handler + ", " + executionSubject + ", " +
                  serviceSubject + ")");

         try
         {
            List<javax.security.auth.callback.Callback> cbs = new ArrayList<javax.security.auth.callback.Callback>();
            cbs.add(new CallerPrincipalCallback(executionSubject, new SimplePrincipal("eis")));
            cbs.add(new GroupPrincipalCallback(executionSubject, new String[] {"eis"}));
            handler.handle(cbs.toArray(new javax.security.auth.callback.Callback[cbs.size()]));
         }
         catch (Throwable t)
         {
            log.error(t.getMessage(), t);
         }
      }
   }
}
