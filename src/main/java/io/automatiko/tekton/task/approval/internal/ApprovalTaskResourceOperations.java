package io.automatiko.tekton.task.approval.internal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.automatiko.tekton.task.approval.ApprovalResults;
import io.automatiko.tekton.task.approval.ApprovalSpec;
import io.automatiko.tekton.task.approval.ApprovalSpec.Strategy;
import io.automatiko.tekton.task.approval.ApprovalStatus;
import io.automatiko.tekton.task.approval.ApprovalTask;
import io.automatiko.tekton.task.run.Run;
import io.automatiko.tekton.task.run.RunSpec;
import io.automatiko.tekton.task.run.RunStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

@ApplicationScoped
public class ApprovalTaskResourceOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalTaskResourceOperations.class);

    @Inject
    KubernetesClient kube;

    @Inject
    Engine templateEngine;

    @PostConstruct
    public void importCustomEmailTemplate() {

        String template = System.getenv("APPROVAL_TASK_EMAIL_SUBJECT");

        if (template != null) {
            Template emailTemplate = templateEngine.parse(template);

            templateEngine.putTemplate("APPROVAL_TASK_EMAIL_SUBJECT", emailTemplate);
            LOGGER.info("Imported template for custom email subject '{}'", template);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean createApprovalTask(Run runResource) {

        Resource<ApprovalTask> r = kube.resources(ApprovalTask.class).inNamespace(runResource.getMetadata().getNamespace())
                .withName(runResource.getMetadata().getName());

        if (r.fromServer().get() == null) {

            RunSpec runSpec = runResource.getSpec();

            ApprovalSpec spec = new ApprovalSpec();

            spec.setDescription((String) runSpec.findParam("description", "empty"));
            spec.setPipeline(
                    (String) runSpec.findParam("pipeline", runResource.getMetadata().getName()));
            spec.setApprovers((List<String>) runSpec.findParam("approvers", Collections.emptyList()));
            spec.setGroups((List<String>) runSpec.findParam("groups", Collections.emptyList()));
            String strategy = (String) runSpec.findParam("strategy", "SINGLE");
            try {

                spec.setStrategy(Strategy.valueOf(strategy));
            } catch (IllegalArgumentException e) {
                // ignore invalid strategies and fallback to use single
                LOGGER.warn("Invalid strategy set '{}', fallback to SINGLE", strategy);
            }

            spec.setData(runSpec.flatParams());

            ApprovalTask item = new ApprovalTask();
            item.getMetadata().setName(runResource.getMetadata().getName());
            item.getMetadata().setNamespace(runResource.getMetadata().getNamespace());

            Map<String, String> labels = new HashMap<>();
            labels.put("decision", "pending");
            if (!spec.getGroups().isEmpty()) {
                labels.put("responses", "0_of_" + spec.getGroups().size());
            } else if (!spec.getApprovers().isEmpty()) {
                labels.put("responses", "0_of_" + spec.getApprovers().size());
            } else {
                labels.put("responses", "0_of_1");
            }

            item.getMetadata().setLabels(labels);
            item.setSpec(spec);
            r.create(item);

            updateRunStatusCreated(runResource);

            LOGGER.info("New approval task has been created {}", runResource.getStatus());
            // returning false to indicate that operator should propagate updates automatically
            return false;
        }
        // return true in case approval task already exists
        return true;
    }

    public boolean onUpdate(Run runResource, boolean skipResourceUpdate) {

        String currentStatus = runResource.getSpec().getStatus();
        if (currentStatus == null) {
            currentStatus = "not set";
        }

        if ("RunCancelled".equalsIgnoreCase(currentStatus)) {
            RunStatus status = runResource.getStatus();
            List<Map<String, Object>> conditions = status.getConditions();

            if (conditions == null) {
                conditions = new ArrayList<>();
                status.setConditions(conditions);
            } else {
                conditions.clear();
            }

            Map<String, Object> initial = new HashMap<>();
            initial.put("status", "False");
            initial.put("reason", "RunTimedOut");
            initial.put("message", "Approval task has timed out");
            initial.put("type", "Succeeded");
            initial.put("lastTransitionTime",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
            conditions.add(initial);

            Resource<ApprovalTask> r = kube.resources(ApprovalTask.class).inNamespace(runResource.getMetadata().getNamespace())
                    .withName(runResource.getMetadata().getName());

            if (r.fromServer().get() != null) {

                r.delete();

                LOGGER.info("Approval task has been deleted {}", runResource.getStatus());
                // returning false to indicate that operator should propagate updates automatically
                return false;
            }
        }
        // return true to indicate no update is needed
        return skipResourceUpdate;
    }

    public void onTimeout(Run runResource) {

        Resource<ApprovalTask> r = kube.resources(ApprovalTask.class).inNamespace(runResource.getMetadata().getNamespace())
                .withName(runResource.getMetadata().getName());

        ApprovalTask fromServer = r.fromServer().get();
        // only delete it when there where no decision taken
        if (fromServer != null && fromServer.getStatus().getResults() == null) {

            r.delete();

            LOGGER.info("Approval task has been deleted {}", runResource.getStatus());

            Resource<Run> rr = kube.resources(Run.class).inNamespace(runResource.getMetadata().getNamespace())
                    .withName(runResource.getMetadata().getName());

            Run instance = rr.fromServer().get();
            if (instance != null) {

                RunStatus status = instance.getStatus();
                List<Map<String, Object>> conditions = status.getConditions();

                if (conditions == null) {
                    conditions = new ArrayList<>();
                    status.setConditions(conditions);
                } else {
                    conditions.clear();
                }

                Map<String, Object> initial = new HashMap<>();
                initial.put("status", "False");
                initial.put("reason", "RunTimedOut");
                initial.put("message", "Approval task has timed out");
                initial.put("type", "Succeeded");
                initial.put("lastTransitionTime",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
                conditions.add(initial);

                rr.patchStatus(instance);
                runResource.setStatus(status);
            }
        }
    }

    public void onDelete(Run runResource) {

        Resource<ApprovalTask> r = kube.resources(ApprovalTask.class).inNamespace(runResource.getMetadata().getNamespace())
                .withName(runResource.getMetadata().getName());
        r.delete();
    }

    public boolean initializeApprovalTask(ApprovalTask resource) {

        ApprovalStatus status = resource.getStatus() == null ? new ApprovalStatus() : resource.getStatus();

        status.setStatus("pending");
        status.setMessage("Approval task has been created and assigned");

        resource.setStatus(status);

        return false;
    }

    public boolean updateApprovalTask(ApprovalTask resource) {

        // do nothing on update events

        return true;
    }

    public boolean deleteApprovalTask(ApprovalTask resource) {

        if (resource.getStatus().getResults() == null) {
            ApprovalStatus status = resource.getStatus() == null ? new ApprovalStatus() : resource.getStatus();

            status.setStatus("cancelled");
            status.setMessage("Approval task has been cancelled");
            if (resource.getMetadata().getLabels() == null) {
                resource.getMetadata().setLabels(new HashMap<>());
            }
            resource.getMetadata().getLabels().put("decision", "cancelled");

            return false;
        }

        return true;
    }

    public void updateApprovalTaskStatusCompleted(ApprovalTask resource, boolean approved, String comment) {
        ApprovalStatus status = resource.getStatus() == null ? new ApprovalStatus() : resource.getStatus();

        status.setStatus("completed");
        status.setMessage("Approval task has been completed");
        status.setApprovalUrl("");

        ApprovalResults results = new ApprovalResults();
        results.setDecision(Boolean.toString(approved));
        results.setComment(comment);
        status.setResults(results);

        resource.setStatus(status);

        Resource<ApprovalTask> r = kube.resources(ApprovalTask.class).inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName());
        ApprovalTask task = r.fromServer().get();

        task.setStatus(resource.getStatus());

        r.patchStatus(task);

        r.edit(ed -> {

            if (ed.getMetadata().getLabels() == null) {
                ed.getMetadata().setLabels(new HashMap<>());
            }
            ed.getMetadata().getLabels().put("decision", approved == true ? "approved" : "rejected");
            resource.getMetadata().setLabels(ed.getMetadata().getLabels());
            resource.setStatus(status);
            return ed;
        });

    }

    public void updateRunResultsCompleted(ApprovalTask resource) {

        Resource<Run> r = kube.resources(Run.class).inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName());

        Run instance = r.fromServer().get();
        if (instance != null) {
            RunStatus status = instance.getStatus() == null ? new RunStatus() : instance.getStatus();

            List<Map<String, Object>> results = new ArrayList<>();
            Map<String, Object> decision = new HashMap<>();
            decision.put("name", "decision");
            decision.put("value", resource.getStatus().getResults().getDecision());
            results.add(decision);

            Map<String, Object> comment = new HashMap<>();
            comment.put("name", "comment");
            comment.put("value", resource.getStatus().getResults().getComment());
            results.add(comment);

            status.setResults(results);

            r.patchStatus(instance);
        }
    }

    public void updateApprovalTaskResponses(ApprovalTask resource) {
        Resource<ApprovalTask> r = kube.resources(ApprovalTask.class).inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName());

        ApprovalTask instance = r.fromServer().get();
        if (instance != null) {
            r.edit(ed -> {
                if (ed.getMetadata().getLabels() == null) {
                    ed.getMetadata().setLabels(new HashMap<>());
                }

                String current = ed.getMetadata().getLabels().get("responses");
                if (current != null) {
                    current = String.valueOf(Integer.valueOf(current.split("_")[0]) + 1);
                } else {
                    current = "0";
                }

                if (!resource.getSpec().getGroups().isEmpty()) {
                    ed.getMetadata().getLabels().put("responses", current + "_of_" + resource.getSpec().getGroups().size());
                } else if (!resource.getSpec().getApprovers().isEmpty()) {
                    ed.getMetadata().getLabels().put("responses", current + "_of_" + resource.getSpec().getApprovers().size());
                } else {
                    ed.getMetadata().getLabels().put("responses", current + "_of_1");
                }
                return ed;
            });
        }
    }

    public void updateRunStatusCreated(Run resource) {

        RunStatus status = resource.getStatus() == null ? new RunStatus() : resource.getStatus();

        List<Map<String, Object>> conditions = status.getConditions();

        if (conditions == null) {
            conditions = new ArrayList<>();
            status.setConditions(conditions);
        }

        Map<String, Object> initial = new HashMap<>();
        initial.put("status", "Unknown");
        initial.put("reason", "Succeeded");
        initial.put("message", "Approval Task has been created");

        conditions.add(initial);

        status.setStartTime(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));

        resource.setStatus(status);
    }

    public void updateRunStatusFailed(Run resource) {

        RunStatus status = resource.getStatus() == null ? new RunStatus() : resource.getStatus();

        List<Map<String, Object>> conditions = status.getConditions();

        if (conditions == null) {
            conditions = new ArrayList<>();
            status.setConditions(conditions);
        }

        Map<String, Object> initial = new HashMap<>();
        initial.put("status", "Failed");
        initial.put("reason", "Failed");
        initial.put("message", "Approval Task failed");
        initial.put("type", "Failed");
        initial.put("lastTransitionTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));

        conditions.add(initial);

        resource.setStatus(status);
    }

    public boolean updateRunStatusCompleted(Run resource) {

        Resource<Run> r = kube.resources(Run.class).inNamespace(resource.getMetadata().getNamespace())
                .withName(resource.getMetadata().getName());

        RunStatus status = resource.getStatus() == null ? new RunStatus() : resource.getStatus();

        List<Map<String, Object>> conditions = status.getConditions();

        if (conditions == null) {
            conditions = new ArrayList<>();
            status.setConditions(conditions);
        } else {
            conditions.clear();
        }

        Map<String, Object> initial = new HashMap<>();
        initial.put("status", "True");
        initial.put("reason", "Succeeded");
        initial.put("message", "Approval Task has been completed");
        initial.put("type", "Succeeded");
        initial.put("lastTransitionTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
        conditions.add(initial);

        r.patchStatus(resource);

        return false;

    }
}
