package fr.opal.type;

import java.util.Date;

/**
 * Represents a project invitation
 */
public class ProjectInvitation {
    private int invitationId;
    private int projectId;
    private String invitedUsername;
    private String inviterUsername;
    private EPermission suggestedPermission;
    private InvitationStatus status;
    private Date sentAt;
    private Date respondedAt;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        CANCELLED
    }

    public ProjectInvitation(int projectId, String invitedUsername, String inviterUsername, EPermission permission) {
        this.projectId = projectId;
        this.invitedUsername = invitedUsername;
        this.inviterUsername = inviterUsername;
        this.suggestedPermission = permission;
        this.status = InvitationStatus.PENDING;
        this.sentAt = new Date();
    }

    // Getters
    public int getInvitationId() {
        return invitationId;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getInvitedUsername() {
        return invitedUsername;
    }

    public String getInviterUsername() {
        return inviterUsername;
    }

    public EPermission getSuggestedPermission() {
        return suggestedPermission;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public Date getRespondedAt() {
        return respondedAt;
    }

    // Setters
    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public void acceptInvitation() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = new Date();
    }

    public void declineInvitation() {
        this.status = InvitationStatus.DECLINED;
        this.respondedAt = new Date();
    }

    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.respondedAt = new Date();
    }

    @Override
    public String toString() {
        return "ProjectInvitation{" +
                "invitationId=" + invitationId +
                ", projectId=" + projectId +
                ", invitedUsername='" + invitedUsername + '\'' +
                ", status=" + status +
                ", sentAt=" + sentAt +
                '}';
    }
}
