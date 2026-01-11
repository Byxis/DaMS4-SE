package fr.opal.type;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project model for project management
 * Represents a project with metadata, collaborators, and permissions
 */
public class Project {
    private int projectId;
    private String name;
    private String description;
    private User owner;
    private Map<String, EPermission> collaborators;  // username -> permission
    private List<String> tags;
    private EProjectState state;
    private Date createdAt;
    private Date updatedAt;

    /**
     * Constructor for creating a new project
     */
    public Project(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.collaborators = new HashMap<>();
        this.collaborators.put(owner.getUsername(), EPermission.OWNER);
        this.tags = new ArrayList<>();
        this.state = EProjectState.PRIVATE;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    /**
     * Constructor for loading from database
     */
    public Project(int projectId, String name, String description, User owner, EProjectState state) {
        this(name, description, owner);
        this.projectId = projectId;
        this.state = state;
    }

    // Getters
    public int getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getOwner() {
        return owner;
    }

    public Map<String, EPermission> getCollaborators() {
        return collaborators;
    }

    public List<String> getTags() {
        return tags;
    }

    public EProjectState getState() {
        return state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = new Date();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = new Date();
    }

    public void setState(EProjectState state) {
        this.state = state;
        this.updatedAt = new Date();
    }

    /**
     * Update project name
     */
    public void updateProjectName(String newName) {
        setName(newName);
    }

    /**
     * Add collaborator to project
     */
    public void addCollaborator(String username, EPermission permission) {
        this.collaborators.put(username, permission);
        this.updatedAt = new Date();
    }

    /**
     * Remove collaborator from project
     */
    public void removeCollaborator(String username) {
        if (!username.equals(owner.getUsername())) {
            this.collaborators.remove(username);
            this.updatedAt = new Date();
        }
    }

    /**
     * Update collaborator permission
     */
    public void updateCollaboratorPermission(String username, EPermission permission) {
        if (this.collaborators.containsKey(username)) {
            this.collaborators.put(username, permission);
            this.updatedAt = new Date();
        }
    }

    /**
     * Check if user is collaborator
     */
    public boolean isCollaborator(String username) {
        return this.collaborators.containsKey(username);
    }

    /**
     * Change project state
     */
    public void changeState(EProjectState newState) {
        setState(newState);
    }

    /**
     * Add tag
     */
    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
            this.updatedAt = new Date();
        }
    }

    /**
     * Remove tag
     */
    public void removeTag(String tag) {
        this.tags.remove(tag);
        this.updatedAt = new Date();
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectId=" + projectId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owner=" + owner.getUsername() +
                ", collaborators=" + collaborators.size() +
                ", state=" + state +
                ", createdAt=" + createdAt +
                '}';
    }
}
