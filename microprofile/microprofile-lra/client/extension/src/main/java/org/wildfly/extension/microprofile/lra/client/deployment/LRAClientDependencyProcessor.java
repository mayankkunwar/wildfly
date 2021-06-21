/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.microprofile.lra.client.deployment;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;

public class LRAClientDependencyProcessor implements DeploymentUnitProcessor {

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        addModuleDependencies(deploymentUnit);
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

    private void addModuleDependencies(DeploymentUnit deploymentUnit) {
        final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();

        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "org.eclipse.microprofile.lra", false, false, true, false));
        moduleSpecification.addSystemDependency(
                cdiDependency(
                        new ModuleDependency(moduleLoader, "io.narayana.lra.client", false, false, true, false)));

        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "org.jboss.narayana.rts.lra-service-base", false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "org.glassfish.jersey.client.JerseyClientBuilder", false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "javax.enterprise.cdi-api", false, false, true, false));
        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "javax.annotation.javax.annotation-api", false, false, true, false));

        moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, "org.jboss.resteasy.client", true, false, true, false));
    }

    private ModuleDependency cdiDependency(ModuleDependency moduleDependency) {
        moduleDependency.addImportFilter(s -> s.equals("META-INF"), true);
        return moduleDependency;
    }
}