package io.automatiko.tekton.task.approval;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApprovalStatus implements KubernetesResource {

    private static final long serialVersionUID = 1L;

    private String approvalUrl;

    private String status;

    private String reason;

    private String message;

    private ApprovalResults results;

    public String getApprovalUrl() {
        return approvalUrl;
    }

    public void setApprovalUrl(String approvalUrl) {
        this.approvalUrl = approvalUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApprovalResults getResults() {
        return results;
    }

    public void setResults(ApprovalResults results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ApprovalStatus [approvalUrl=" + approvalUrl + ", status=" + status + ", reason=" + reason + ", message="
                + message + ", results=" + results + "]";
    }
}
