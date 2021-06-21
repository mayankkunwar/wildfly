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

package org.wildfly.extension.microprofile.lra.client;

import static org.jboss.as.controller.OperationContext.Stage.RUNTIME;
import static org.jboss.as.server.deployment.Phase.DEPENDENCIES;
import static org.wildfly.extension.microprofile.lra.client.MicroProfileLRAClientExtension.SUBSYSTEM_NAME;
import static org.wildfly.extension.microprofile.lra.client.MicroProfileLRAClientExtension.SUBSYSTEM_PATH;

import java.util.Collection;
import java.util.Collections;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.RuntimePackageDependency;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.microprofile.lra.client._private.MicroProfileLRAClientLogger;
import org.wildfly.extension.microprofile.lra.client.deployment.LRAClientDependencyProcessor;


public class MicroProfileLRAClientSubsystemDefinition extends PersistentResourceDefinition {

    private static final String LRA_CLIENT_CAPABILITY_NAME = "org.wildfly.microprofile.lra.client";

    private static final RuntimeCapability<Void> LRA_CLIENT_CAPABILITY = RuntimeCapability.Builder
            .of(LRA_CLIENT_CAPABILITY_NAME)
            .addRequirements(LRA_CLIENT_CAPABILITY_NAME)
            .build();

    public MicroProfileLRAClientSubsystemDefinition() {
        super(
                new SimpleResourceDefinition.Parameters(
                        SUBSYSTEM_PATH,
                        MicroProfileLRAClientExtension.getResourceDescriptionResolver(SUBSYSTEM_NAME))
                        .setAddHandler(AddHandler.INSTANCE)
                        .setRemoveHandler(new ReloadRequiredRemoveStepHandler())
                        .setCapabilities(LRA_CLIENT_CAPABILITY)
        );
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Collections.emptyList();
    }

    @Override
    public void registerAdditionalRuntimePackages(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerAdditionalRuntimePackages(
                RuntimePackageDependency.required("io.narayana.lra.client"),
                RuntimePackageDependency.required("org.eclipse.microprofile.lra"),
                RuntimePackageDependency.required("org.jboss.narayana.rts.lra-service-base"),
                RuntimePackageDependency.required("javax.enterprise.cdi-api"),
                RuntimePackageDependency.required("javax.annotation.javax.annotation-api"),
                RuntimePackageDependency.required("org.jboss.resteasy.client"),
                RuntimePackageDependency.required("org.slf4j"));

    }

    static class AddHandler extends AbstractBoottimeAddStepHandler {

        static AddHandler INSTANCE = new AddHandler();

        private AddHandler() {
            super(Collections.emptyList());
        }

        @Override
        protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
            super.performBoottime(context, operation, model);

            context.addStep(new AbstractDeploymentChainStep() {
                public void execute(DeploymentProcessorTarget processorTarget) {

                    // TODO Put these into Phase.java https://issues.redhat.com/browse/WFCORE-5217
                    final int DEPENDENCIES_MICROPROFILE_LRA_CLIENT = 0x18D0;

                    processorTarget.addDeploymentProcessor(SUBSYSTEM_NAME, DEPENDENCIES, DEPENDENCIES_MICROPROFILE_LRA_CLIENT, new LRAClientDependencyProcessor());
                }
            }, RUNTIME);

            MicroProfileLRAClientLogger.LOGGER.activatingSubsystem();
        }
    }
}