package io.automatiko.tekton.task.approval;

import java.util.List;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
public class ApprovalSpec implements KubernetesResource {

    private static final long serialVersionUID = 1L;

    public enum Strategy {
        SINGLE,
        MULTI
    }

    private String description;
    private List<String> approvers;
    private String pipeline;
    private Strategy strategy;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getApprovers() {
        return approvers;
    }

    public void setApprovers(List<String> recipient) {
        this.approvers = recipient;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "ApprovalSpec [description=" + description + ", approvers=" + approvers + ", pipeline=" + pipeline
                + ", strategy=" + strategy + "]";
    }

}
