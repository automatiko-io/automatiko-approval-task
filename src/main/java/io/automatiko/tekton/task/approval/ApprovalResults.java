package io.automatiko.tekton.task.approval;

public class ApprovalResults {

    private String decision;

    private String comment;

    private String approvedBy;

    private String rejectedBy;

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

    public ApprovalResults(String decision, String comment, String approvedBy, String rejectedBy) {
        this.decision = decision;
        this.comment = comment;
        this.approvedBy = approvedBy;
        this.rejectedBy = rejectedBy;
    }

    public ApprovalResults(boolean decision, String comment, String approvedBy, String rejectedBy) {
        this.decision = Boolean.toString(decision);
        this.comment = comment;
        this.approvedBy = approvedBy;
        this.rejectedBy = rejectedBy;
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    @Override
    public String toString() {
        return "ApprovalResults [decision=" + decision + ", comment=" + comment + ", approvedBy=" + approvedBy + ", rejectedBy="
                + rejectedBy + "]";
    }

}
