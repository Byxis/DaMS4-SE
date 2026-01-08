# Granular Saving & Permission Editing Implementation

## Overview
This document describes the implementation of granular saving with strict permission checks, separating comment persistence (auto-save) from entry data/permission persistence (manual save).

---

## 1. EntryManager Methods

### Comment Management (Auto-Save)

#### `addComment(int entryId, Comment comment) throws PermissionException`
**Purpose:** Add a comment to an entry with immediate persistence (Auto-Save)

**Permission Check:**
- Requires: `COMMENTOR` or `EDITOR`
- Blocks: `READER` users

**Security Flow:**
```java
public void addComment(int entryId, Comment comment) throws PermissionException {
    // Load entry with full details
    Entry entry = dao.loadEntryWithDetails(entryId);
    if (entry == null) {
        throw new PermissionException("Entry not found");
    }
    
    // SECURITY: Verify COMMENTOR permission (or higher)
    if (!hasPermission(entry, EPermission.COMMENTOR)) {
        throw new PermissionException("You do not have permission to comment on this entry");
    }
    
    // Persist ONLY the comment (not the entire entry)
    entry.addComment(comment);
    dao.saveEntry(entry);
}
```

**Characteristics:**
- Immediate persistence (Auto-Save)
- Only comment is saved, not entry data
- Permission check at method entry

---

### Entry Data & Permission Updates (Manual Save)

#### `updateEntry(int entryId, String newTitle, String newContent, List<UserPermission> permissionOverrides) throws PermissionException`
**Purpose:** Update entry data AND permissions with explicit user action (Manual Save)

**Permission Checks:**
- Requires: `EDITOR` only
- Blocks: `READER` and `COMMENTOR` users (Strict)

**Security Flow:**
```java
public void updateEntry(int entryId, String newTitle, String newContent, 
                       List<UserPermission> permissionOverrides) throws PermissionException {
    // Load entry with full details
    Entry entry = dao.loadEntryWithDetails(entryId);
    if (entry == null) {
        throw new PermissionException("Entry not found");
    }
    
    // STRICT CHECK #1: Explicitly block READER users
    UserPermission userPerm = entry.getPermissionManager().getUserPermission(currentUser);
    if (userPerm != null && userPerm.getPermission() == EPermission.READER) {
        throw new PermissionException("Read-only users cannot modify entries");
    }
    
    // STRICT CHECK #2: Only EDITOR can proceed
    if (!hasPermission(entry, EPermission.EDITOR)) {
        throw new PermissionException("You do not have editor permissions for this entry");
    }
    
    // Update entry data
    entry.setTitle(newTitle);
    entry.setContent(newContent);
    
    // Update permissions if provided
    if (permissionOverrides != null && !permissionOverrides.isEmpty()) {
        for (UserPermission perm : permissionOverrides) {
            entry.getPermissionManager().addUserPermission(perm);
        }
    }
    
    // Persist the complete entry with all changes
    persistEntry(entry);
}
```

**Characteristics:**
- Manual save (requires explicit button click)
- Saves: Title, Content, Permissions
- Double security checks (explicit READER block + EDITOR requirement)
- Atomic update (all changes persisted together)

---

## 2. Permission Hierarchy

```
READER (Viewer)
  ↑
  Cannot modify anything
  Cannot add comments
  Cannot change permissions

COMMENTOR
  ↑
  Can add comments (Auto-Save)
  Cannot modify entry data
  Cannot change permissions

EDITOR
  ↑
  Can modify all (entry data, permissions)
  Can add comments
  Can delete comments
  Can change other users' permissions
```

---

## 3. Frontend Controller Logic

### EntryController Implementation

#### onAddComment() - Auto-Save Flow
```java
@FXML
public void onAddComment() {
    Entry current = entryManager.getCurrentEntry();
    if (current == null) {
        showErrorDialog("Error", "No entry selected");
        return;
    }
    
    String commentText = commentInput.getText().trim();
    if (commentText.isEmpty()) {
        showErrorDialog("Error", "Comment cannot be empty");
        return;
    }
    
    try {
        // Create comment with current user as author
        Comment comment = new Comment(commentText, currentUser);
        
        // Call the new addComment method
        // Throws PermissionException if user is not COMMENTOR or EDITOR
        entryManager.addComment(current.getId(), comment);
        
        // Clear input and refresh display
        commentInput.clear();
        displayComments(current);
        showInfoDialog("Success", "Comment added successfully");
    } catch (EntryManager.PermissionException e) {
        showErrorDialog("Permission Denied", e.getMessage());
    } catch (Exception e) {
        showErrorDialog("Error adding comment", e.getMessage());
    }
}
```

**Auto-Save Characteristics:**
- User clicks "Post Comment" button
- Comment is immediately persisted to database
- No intermediate "Save" step required
- Instant feedback if permission denied

---

#### onSaveEntry() - Manual Save Flow
```java
@FXML
public void onSaveEntry() {
    Entry current = entryManager.getCurrentEntry();
    if (current == null) {
        showErrorDialog("Error", "No entry selected");
        return;
    }
    
    try {
        String newTitle = entryTitleField.getText();
        String newContent = entryContent.getText();
        
        // Call the new updateEntry method with permission checks
        // Throws PermissionException if user is not EDITOR
        entryManager.updateEntry(current.getId(), newTitle, newContent, null);
        
        showInfoDialog("Success", "Entry saved successfully");
        
        // Reload the entry to refresh UI with fresh data from database
        loadAndDisplayEntry(current.getId());
    } catch (EntryManager.PermissionException e) {
        showErrorDialog("Permission Denied", e.getMessage());
    } catch (Exception e) {
        showErrorDialog("Error saving entry", e.getMessage());
    }
}
```

**Manual Save Characteristics:**
- User explicitly clicks "Save Entry" button
- Only EDITOR can save (COMMENTOR and READER get denied)
- Saves Title + Content + Permissions in one atomic operation
- UI refreshes from database after successful save

---

## 4. UI State Management

### Button Enable/Disable Logic

In `displayEntry()`:
```java
// Get effective permission for UI restrictions
UserPermission userPerm = entry.getUserPermissionWithCascade(currentUser);
EPermission permission = userPerm.getPermission();
boolean isEditor = permission.canEdit();
boolean isCommenter = permission.canComment();

// Disable save button and lock content fields if not editor
entryTitleField.setDisable(!isEditor);
entryContent.setDisable(!isEditor);
saveEntryBtn.setDisable(!isEditor);

// Lock comment input and button for readers
commentInput.setDisable(!isCommenter);
addCommentBtn.setDisable(!isCommenter);
```

**Result:**
- `READER`: All input fields locked, all buttons disabled
- `COMMENTOR`: Can only comment, entry fields locked, save button disabled
- `EDITOR`: All fields and buttons enabled

---

## 5. Safety Net: The "Defense in Depth" Approach

### Frontend Defense (UI Layer)
- Buttons disabled for users without permission
- Input fields read-only
- Clear visual feedback

### Backend Defense (Service Layer)
- `addComment()`: Checks `COMMENTOR` permission
- `updateEntry()`: Explicit `READER` block + `EDITOR` requirement
- Exceptions thrown with descriptive messages
- No silent failures

### Database Defense (DAO Layer)
- Foreign key constraints
- NOT NULL constraints on critical fields
- Transactional updates

---

## 6. Error Handling Strategy

### PermissionException
A custom exception thrown by EntryManager methods when security checks fail:

```java
public static class PermissionException extends Exception {
    public PermissionException(String message) {
        super(message);
    }
}
```

**Typical Messages:**
- "You do not have permission to comment on this entry"
- "Read-only users cannot modify entries"
- "You do not have editor permissions for this entry"

### Controller Error Handling
```java
catch (EntryManager.PermissionException e) {
    // User-friendly permission denied message
    showErrorDialog("Permission Denied", e.getMessage());
} catch (Exception e) {
    // Catch unexpected errors
    showErrorDialog("Error", e.getMessage());
}
```

---

## 7. Data Flow Diagrams

### Auto-Save Flow (Comments)
```
User Types Comment
        ↓
User Clicks "Post Comment"
        ↓
onAddComment() called
        ↓
Create Comment object (with currentUser as author)
        ↓
entryManager.addComment(entryId, comment)
        ↓
[SECURITY CHECK] Has COMMENTOR permission?
        ↓ YES                              ↓ NO
        ↓                           PermissionException
dao.saveEntry(entry)                      ↓
        ↓                     showErrorDialog("Permission Denied")
Comment persisted to database
        ↓
displayComments(current) - refresh UI
        ↓
showInfoDialog("Success")
```

### Manual Save Flow (Entry Data & Permissions)
```
User Edits Title/Content
        ↓
User Clicks "Save Entry"
        ↓
onSaveEntry() called
        ↓
entryManager.updateEntry(entryId, newTitle, newContent, permissions)
        ↓
[SECURITY CHECK #1] Is user READER?
        ↓ YES                              ↓ NO
        ↓                                  ↓
PermissionException                [SECURITY CHECK #2]
        ↓                          Has EDITOR permission?
showErrorDialog                    ↓ YES                ↓ NO
                                   ↓                 PermissionException
                            Update entry data           ↓
                                   ↓            showErrorDialog
                            Update permissions
                                   ↓
                            persistEntry(entry)
                                   ↓
                         Entry persisted to database
                                   ↓
                        loadAndDisplayEntry() - refresh
                                   ↓
                        showInfoDialog("Success")
```

---

## 8. Summary Table

| Feature | Comment | Entry Data & Permissions |
|---------|---------|--------------------------|
| **Method** | `addComment(entryId, comment)` | `updateEntry(entryId, title, content, perms)` |
| **Trigger** | Auto-Save (immediate) | Manual Save (button click) |
| **Permission** | COMMENTOR or EDITOR | EDITOR only |
| **Blocks** | READER | READER, COMMENTOR |
| **Scope** | Comment only | Title, Content, Permissions |
| **Security Checks** | 1 check (COMMENTOR) | 2 checks (explicit READER block + EDITOR) |
| **UI Button** | "Post Comment" | "Save Entry" |
| **Feedback** | Immediate success/error | Reload from database |
