package io.automatiko.tekton.task.approval;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.automatiko.engine.api.Functions;
import io.automatiko.engine.api.runtime.process.ProcessContext;
import io.automatiko.tekton.task.run.CustomRun;
import io.automatiko.tekton.task.run.CustomRunStatus;
import io.fabric8.kubernetes.api.model.Duration;

public class ApprovalFunctions implements Functions {

    public static String approversAsString(ApprovalTask resource) {
        if (resource.getSpec().getApprovers() == null || resource.getSpec().getApprovers().isEmpty()) {
            return "";
        }

        return resource.getSpec().getApprovers().stream().collect(Collectors.joining(","));
    }

    public static String groupsAsString(ApprovalTask resource) {
        if (resource.getSpec().getGroups() == null || resource.getSpec().getGroups().isEmpty()) {
            return "";
        }

        return resource.getSpec().getGroups().stream().collect(Collectors.joining(","));
    }

    public static boolean isGroupApproval(ApprovalTask resource) {
        if (resource.getSpec().getGroups() != null && !resource.getSpec().getGroups().isEmpty()) {
            return true;
        }

        return false;
    }

    public static boolean hasApprovalTaskRef(CustomRun eventData) {
        if (eventData.getSpec() != null) {
            String apiVersion = (String) eventData.getSpec().getCustomRef().getApiVersion();
            String kind = (String) eventData.getSpec().getCustomRef().getKind();

            if (ApprovalTask.API_VERSION.equalsIgnoreCase(apiVersion) && ApprovalTask.KIND.equalsIgnoreCase(kind)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("rawtypes")
    public static boolean hasStatusSet(CustomRun resource) {
        if (resource.getStatus() == null) {
            return false;
        }

        List<Map<String, Object>> conditions = resource.getStatus().getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        for (Object condition : conditions) {
            if (condition instanceof Map) {
                Object statusSet = ((Map) condition).get("status");
                if (statusSet != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasResults(CustomRun resource) {
        if (resource.getStatus() == null) {
            return false;
        }

        if (resource.getStatus().getResults() == null) {
            return false;
        }

        return resource.getStatus().getResults().size() > 0;
    }

    public static boolean isCompleted(CustomRun resource) {
        if (resource.getStatus() == null) {
            return false;
        }

        CustomRunStatus status = resource.getStatus();

        List<Map<String, Object>> conditions = status.getConditions();

        if (conditions == null) {
            return false;
        } else {
            String currentStatus = (String) conditions.stream().filter(item -> item.containsKey("status"))
                    .map(item -> item.get("status")).findFirst()
                    .orElse(null);

            if (currentStatus == null || currentStatus.equalsIgnoreCase("Unknown")) {
                return false;
            }

            return true;
        }
    }

    public static boolean hasStatusSet(ApprovalTask resource) {
        if (resource.getStatus() == null) {
            return false;
        }

        if (resource.getStatus().getStatus() == null || resource.getStatus().getStatus().isEmpty()) {
            return false;
        }

        return true;
    }

    public static boolean hasResults(ApprovalTask resource) {
        if (resource.getStatus() == null) {
            return false;
        }

        if (resource.getStatus().getResults() == null) {
            return false;
        }

        return resource.getStatus().getResults().getDecision() != null;
    }

    public static String approvalTaskTag(CustomRun resource) {
        return resource.getMetadata().getName();
    }

    public static String pipelineTag(CustomRun resource) {
        return (String) resource.getSpec().findParam("pipeline", "not set");
    }

    public static String runInstanceDescription(CustomRun resource) {
        return "Approval requested for '" + pipelineTag(resource) + "'";
    }

    public static String pipelineTag(ApprovalTask resource) {
        return resource.getSpec().getPipeline();
    }

    public static String decisionTag(ApprovalTask resource) {
        String currentStatus = "pending";
        if (resource.getStatus() != null && resource.getStatus().getStatus() != null) {

            if (Boolean.parseBoolean(resource.getStatus().getResults().getDecision())) {
                currentStatus = "approved";
            } else {
                currentStatus = "rejected";
            }
        }
        return currentStatus;
    }

    public static String responsesTag(ApprovalTask resource) {
        return "responses " + resource.getMetadata().getLabels().getOrDefault("responses", "-").replaceAll("_", " ");
    }

    public static String approvalInstanceDescription(ApprovalTask resource) {
        return "Approval Task for '" + pipelineTag(resource) + "'";
    }

    public static boolean isSingleApprovalStrategy(ApprovalTask resource) {

        if (resource.getSpec().getStrategy() == null || resource.getSpec().getStrategy().equals(ApprovalSpec.Strategy.SINGLE)) {
            return true;
        }

        return false;
    }

    public static boolean isMultiApprovalStrategy(ApprovalTask resource) {

        if (resource.getSpec().getStrategy() != null && resource.getSpec().getStrategy().equals(ApprovalSpec.Strategy.MULTI)) {
            return true;
        }

        return false;
    }

    public static boolean isFourEyesApprovalStrategy(ApprovalTask resource) {

        if (resource.getSpec().getStrategy() == null
                || resource.getSpec().getStrategy().equals(ApprovalSpec.Strategy.FOUR_EYES)) {
            return true;
        }

        return false;
    }

    public static void calculateOutcome(List<ApprovalResults> results, ProcessContext context) {
        boolean decision = results.stream().allMatch(result -> "true".equalsIgnoreCase(result.getDecision()));
        String comment = results.stream().map(result -> result.getComment()).filter(c -> c != null)
                .collect(Collectors.joining("|"));

        context.setVariable("comment", comment);
        context.setVariable("approved", decision);
    }

    public static String kubeDurationToIso(CustomRun run) {
        try {
            Duration duration = Duration.parse(run.getSpec().getTimeout());
            return duration.getDuration().toString();
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean hasTimer(CustomRun resource) {
        if (resource.getSpec().getTimeout() != null && !resource.getSpec().getTimeout().isEmpty()) {
            return true;
        }

        return false;
    }

    public static boolean isAlreadyRejected(Collection<ApprovalResults> results) {
        if (results != null) {
            return results.stream().anyMatch(r -> Boolean.parseBoolean(r.getDecision()) == false);
        }

        return false;
    }

    public static String emailSubject(ApprovalTask resource) {
        if (resource.getSpec().getDescription() != null && !resource.getSpec().getDescription().trim().isEmpty()) {

            return resource.getSpec().getDescription();
        }

        return "Approval required for pipeline " + resource.getSpec().getPipeline();
    }

    public static String teamsChannel(ApprovalTask resource) {
        if (resource.getSpec().getNotifyOnTeams() != null) {
            return resource.getSpec().getNotifyOnTeams();
        }
        return null;
    }

    public static String slackChannel(ApprovalTask resource) {
        if (resource.getSpec().getNotifyOnSlack() != null) {
            return resource.getSpec().getNotifyOnSlack();
        }
        return null;
    }
}
