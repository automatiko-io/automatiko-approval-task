package io.automatiko.tekton.task.approval;

public class ApprovalResults {

    private String decision;

    private String comment;

    public ApprovalResults() {

    }

    public ApprovalResults(String decision, String comment) {
        this.decision = decision;
        this.comment = comment;
    }

    public ApprovalResults(boolean decision, String comment) {
        this.decision = Boolean.toString(decision);
        this.comment = comment;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ApprovalResults [decision=" + decision + ", comment=" + comment + "]";
    }

}
