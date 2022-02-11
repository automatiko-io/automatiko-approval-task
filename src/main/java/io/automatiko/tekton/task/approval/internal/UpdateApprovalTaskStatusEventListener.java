package io.automatiko.tekton.task.approval.internal;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;

import io.automatiko.engine.api.event.process.DefaultProcessEventListener;
import io.automatiko.engine.api.event.process.ProcessNodeTriggeredEvent;
import io.automatiko.engine.workflow.process.instance.node.HumanTaskNodeInstance;
import io.automatiko.tekton.task.approval.ApprovalTask;

@ApplicationScoped
public class UpdateApprovalTaskStatusEventListener extends DefaultProcessEventListener {

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        if (event.getNodeInstance() instanceof HumanTaskNodeInstance) {
            HumanTaskNodeInstance htNodeInstance = (HumanTaskNodeInstance) event.getNodeInstance();
            if (event.getProcessInstance().getProcessId().equals("single")
                    && htNodeInstance.getWorkItem().getParameter("TaskName").equals("approval")) {

                String url = ConfigProvider.getConfig().getOptionalValue("quarkus.automatiko.service-url", String.class)
                        .orElse("http://localhost:8080")
                        + htNodeInstance.buildFormLink();

                ApprovalTask approvalTask = (ApprovalTask) event.getProcessInstance().getVariable("resource");
                approvalTask.getStatus().setApprovalUrl(url);
            } else if (event.getProcessInstance().getProcessId().equals("multi")
                    && htNodeInstance.getWorkItem().getParameter("TaskName").equals("approval")) {
                ApprovalTask approvalTask = (ApprovalTask) event.getProcessInstance().getVariable("resource");

                String url = ConfigProvider.getConfig().getOptionalValue("quarkus.automatiko.service-url", String.class)
                        .orElse("http://localhost:8080")
                        + "/index.html#" + approvalTask.getSpec().getPipeline();

                approvalTask.getStatus().setApprovalUrl(url);
            }
        }
    }

}
