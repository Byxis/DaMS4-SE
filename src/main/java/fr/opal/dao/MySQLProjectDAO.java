package fr.opal.dao;

import fr.opal.exception.ProjectException;
import fr.opal.type.EPermission;
import fr.opal.type.Project;
import fr.opal.type.ProjectInvitation;
import fr.opal.type.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL implementation of ProjectDAO
 * Implements all abstract methods from ProjectDAO
 */
public class MySQLProjectDAO extends ProjectDAO {

    private Connection conn;

    /**
     * Default constructor
     *
     * @param _conn the database connection
     */
    public MySQLProjectDAO(Connection _conn)
    {
        this.conn = _conn;
    }

    /**
     * Retrieves a project by its ID
     *
     * @param projectId the project ID
     * @return the project or null if not found
     */
    @Override
    public Project getProjectById(int projectId)
    {
        String sql = "SELECT p.id, p.name, p.description, p.owner_id, p.state, u.username FROM projects p JOIN users u ON p.owner_id = u.id WHERE p.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, projectId);
            var rs = ps.executeQuery();
            if (rs.next())
            {
                return buildProjectFromResultSet(rs);
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving project: " + projectId, e);
        }
        return null;
    }

    /**
     * Saves a project to the database
     *
     * @param project the project to save
     */
    @Override
    public void saveProject(Project project)
    {
        String sql = "UPDATE projects SET name = ?, description = ?, owner_id = ?, state = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setInt(3, project.getOwner().getId());
            ps.setString(4, project.getState().toString());
            ps.setInt(5, project.getProjectId());
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error saving project: " + project.getProjectId(), e);
        }
    }

    /**
     * Creates a new project in the database and returns its ID
     *
     * @param project the project to create
     * @return the generated project ID
     */
    @Override
    public int createProject(Project project)
    {
        String sql = "INSERT INTO projects(name, description, owner_id, state) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setInt(3, project.getOwner().getId());
            ps.setString(4, project.getState().toString());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error creating project: " + project.getName(), e);
        }
        return 0;
    }

    /**
     * Deletes a project from the database
     *
     * @param projectId the project ID
     */
    @Override
    public void deleteProject(int projectId)
    {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, projectId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error deleting project: " + projectId, e);
        }
    }

    /**
     * Retrieves all projects
     *
     * @return list of all projects
     */
    @Override
    public List<Project> getAllProjects()
    {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description, p.owner_id, p.state, u.username FROM projects p JOIN users u ON p.owner_id = u.id";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            var rs = ps.executeQuery();
            while (rs.next())
            {
                projects.add(buildProjectFromResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving all projects", e);
        }
        return projects;
    }

    /**
     * Retrieves projects by owner
     *
     * @param ownerUsername the owner username
     * @return list of projects owned by the user
     */
    @Override
    public List<Project> getProjectsByOwner(String ownerUsername)
    {
        List<Project> projects = new ArrayList<>();
        // Query by joining users table to match username to owner_id
        String sql = "SELECT p.id, p.name, p.description, p.owner_id, p.state, u.username FROM projects p JOIN users u ON p.owner_id = u.id WHERE u.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, ownerUsername);
            var rs = ps.executeQuery();
            while (rs.next())
            {
                projects.add(buildProjectFromResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving projects for owner: " + ownerUsername, e);
        }
        return projects;
    }

    /**
     * Retrieves projects for a collaborator
     *
     * @param username the collaborator username
     * @return list of projects the user collaborates on
     */
    @Override
    public List<Project> getProjectsForCollaborator(String username)
    {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description, p.owner_id, p.state, u.username FROM projects p JOIN project_collaborators pc ON p.id = pc.project_id JOIN users u ON p.owner_id = u.id WHERE pc.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            while (rs.next())
            {
                projects.add(buildProjectFromResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving projects for collaborator: " + username, e);
        }
        return projects;
    }

    /**
     * Searches projects by name, description, tags, or owner
     *
     * @param keyword the search keyword
     * @return list of projects matching the keyword
     */
    @Override
    public List<Project> searchProjects(String keyword)
    {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description, p.owner_id, p.state, u.username FROM projects p JOIN users u ON p.owner_id = u.id WHERE p.name LIKE ? OR p.description LIKE ? OR u.username LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            String searchTerm = "%" + keyword + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);
            var rs = ps.executeQuery();
            while (rs.next())
            {
                projects.add(buildProjectFromResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error searching projects: " + keyword, e);
        }
        return projects;
    }

    /**
     * Retrieves projects with a specific tag
     *
     * @param tag the tag to search for
     * @return list of projects with the tag
     */
    @Override
    public List<Project> getProjectsByTag(String tag)
    {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.id, p.name, p.description, p.owner_id, p.state, u.username FROM projects p JOIN project_tags pt ON p.id = pt.project_id JOIN users u ON p.owner_id = u.id WHERE pt.tag = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, tag);
            var rs = ps.executeQuery();
            while (rs.next())
            {
                projects.add(buildProjectFromResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving projects by tag: " + tag, e);
        }
        return projects;
    }

    /**
     * Saves project collaborators to database
     *
     * @param projectId the project ID
     * @param collaborators map of collaborators
     */
    @Override
    public void saveProjectCollaborators(int projectId, Map<String, EPermission> collaborators)
    {
        String deleteSql = "DELETE FROM project_collaborators WHERE project_id = ?";
        try (PreparedStatement deletePs = conn.prepareStatement(deleteSql))
        {
            deletePs.setInt(1, projectId);
            deletePs.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error deleting project collaborators: " + projectId, e);
        }

        String insertSql = "INSERT INTO project_collaborators(project_id, username, permission) VALUES (?, ?, ?)";
        try (PreparedStatement insertPs = conn.prepareStatement(insertSql))
        {
            for (Map.Entry<String, EPermission> entry : collaborators.entrySet())
            {
                insertPs.setInt(1, projectId);
                insertPs.setString(2, entry.getKey());
                insertPs.setString(3, entry.getValue().toString());
                insertPs.addBatch();
            }
            insertPs.executeBatch();
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error saving project collaborators: " + projectId, e);
        }
    }

    /**
     * Loads project collaborators from database
     *
     * @param projectId the project ID
     * @return map of collaborators
     */
    @Override
    public Map<String, EPermission> loadProjectCollaborators(int projectId)
    {
        Map<String, EPermission> collaborators = new HashMap<>();
        String sql = "SELECT username, permission FROM project_collaborators WHERE project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, projectId);
            var rs = ps.executeQuery();
            while (rs.next())
            {
                String username = rs.getString("username");
                String permission = rs.getString("permission");
                collaborators.put(username, EPermission.valueOf(permission));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error loading project collaborators: " + projectId, e);
        }
        return collaborators;
    }

    /**
     * Updates project relationships in the database
     *
     * @param project the project with updated relationships
     */
    @Override
    public void updateProjectRelationships(Project project)
    {
        saveProjectCollaborators(project.getProjectId(), project.getCollaborators());
    }

    /**
     * Loads all project data including collaborators
     *
     * @param projectId the project ID
     * @return the project with all details or null if not found
     */
    @Override
    public Project loadProjectWithDetails(int projectId)
    {
        Project project = getProjectById(projectId);
        if (project != null)
        {
            Map<String, EPermission> collaborators = loadProjectCollaborators(projectId);
            project.getCollaborators().putAll(collaborators);
        }
        return project;
    }

    /**
     * Creates a new invitation
     *
     * @param invitation the invitation to create
     * @return the generated invitation ID
     */
    @Override
    public int createInvitation(ProjectInvitation invitation)
    {
        String sql = "INSERT INTO project_invitations(project_id, invited_username, inviter_username, suggested_permission, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setInt(1, invitation.getProjectId());
            ps.setString(2, invitation.getInvitedUsername());
            ps.setString(3, invitation.getInviterUsername());
            ps.setString(4, invitation.getSuggestedPermission().toString());
            ps.setString(5, invitation.getStatus().toString());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys())
            {
                if (rs.next())
                {
                    return rs.getInt(1);
                }
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error creating invitation: " + invitation.getInvitedUsername(), e);
        }
        return 0;
    }

    /**
     * Retrieves pending invitations for a user
     *
     * @param username the username
     * @return list of pending invitations
     */
    @Override
    public List<ProjectInvitation> getPendingInvitations(String username)
    {
        List<ProjectInvitation> invitations = new ArrayList<>();
        String sql = "SELECT id, project_id, invited_username, inviter_username, suggested_permission, status FROM project_invitations WHERE invited_username = ? AND status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            while (rs.next())
            {
                invitations.add(buildInvitationFromResultSet(rs));
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving pending invitations for: " + username, e);
        }
        return invitations;
    }

    /**
     * Retrieves an invitation by its ID
     *
     * @param invitationId the invitation ID
     * @return the invitation or null if not found
     */
    @Override
    public ProjectInvitation getInvitation(int invitationId)
    {
        String sql = "SELECT id, project_id, invited_username, inviter_username, suggested_permission, status FROM project_invitations WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setInt(1, invitationId);
            var rs = ps.executeQuery();
            if (rs.next())
            {
                return buildInvitationFromResultSet(rs);
            }
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error retrieving invitation: " + invitationId, e);
        }
        return null;
    }

    /**
     * Updates invitation status
     *
     * @param invitationId the invitation ID
     * @param status the new status
     */
    @Override
    public void updateInvitationStatus(int invitationId, ProjectInvitation.InvitationStatus status)
    {
        String sql = "UPDATE project_invitations SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, status.toString());
            ps.setInt(2, invitationId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new ProjectException("Error updating invitation status: " + invitationId, e);
        }
    }

    /**
     * Helper method to build a Project from a ResultSet
     *
     * @param rs the ResultSet
     * @return the Project object
     * @throws SQLException if a database access error occurs
     */
    private Project buildProjectFromResultSet(ResultSet rs) throws SQLException
    {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int ownerId = rs.getInt("owner_id");
        String state = rs.getString("state");
        String ownerUsername = rs.getString("username");

        // Create owner object with ID and username
        User owner = new User(ownerId, ownerUsername, "");
        
        Project project = new Project(name, description, owner);
        project.setProjectId(id);
        return project;
    }

    /**
     * Helper method to build a ProjectInvitation from a ResultSet
     *
     * @param rs the ResultSet
     * @return the ProjectInvitation object
     * @throws SQLException if a database access error occurs
     */
    private ProjectInvitation buildInvitationFromResultSet(ResultSet rs) throws SQLException
    {
        int id = rs.getInt("id");
        int projectId = rs.getInt("project_id");
        String invitedUsername = rs.getString("invited_username");
        String inviterUsername = rs.getString("inviter_username");
        String permission = rs.getString("suggested_permission");
        String status = rs.getString("status");

        EPermission perm = null;
        if (permission != null) {
            try {
                perm = EPermission.valueOf(permission);
            } catch (IllegalArgumentException e) {
                perm = EPermission.CONTRIBUTOR;
            }
        }
        
        ProjectInvitation invitation = new ProjectInvitation(projectId, invitedUsername, inviterUsername, perm);
        invitation.setInvitationId(id);
        return invitation;
    }
}
