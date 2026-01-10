-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User profiles table
CREATE TABLE IF NOT EXISTS user_profiles (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    contact_info VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Session settings table (user appearance preferences)
CREATE TABLE IF NOT EXISTS session_settings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    font_size INT DEFAULT 14,
    style_palette VARCHAR(20) DEFAULT 'LIGHT',
    accent_color VARCHAR(20) DEFAULT 'BLACK',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Friendships table (bidirectional friend relationships)
-- A friendship owns exactly one Channel (Private DM).
-- Note: 'channel_id' is NULL while status is PENDING, populated when ACCEPTED.
CREATE TABLE IF NOT EXISTS friendships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id1 INT NOT NULL,
    user_id2 INT NOT NULL,
    channel_id INT UNIQUE NULL,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id1) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id2) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE SET NULL,
    UNIQUE KEY unique_friendship (user_id1, user_id2),
    CHECK (user_id1 != user_id2)
) ENGINE=InnoDB;

-- Follows table (unidirectional follow relationships)
CREATE TABLE IF NOT EXISTS follows (
    id INT PRIMARY KEY AUTO_INCREMENT,
    follower_id INT NOT NULL,
    followed_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_follow (follower_id, followed_id),
    CHECK (follower_id != followed_id)
);

-- Blocks table (user blocking relationships)
CREATE TABLE IF NOT EXISTS blocks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    blocker_id INT NOT NULL,
    blocked_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_block (blocker_id, blocked_id),
    CHECK (blocker_id != blocked_id)
) ENGINE=InnoDB;

-- ====================================
-- UNIFIED CHANNEL ARCHITECTURE
-- ====================================

-- 1. Channels (The Generic Container)
-- Strictly a bucket for messages. No metadata (names/types) allowed here.
CREATE TABLE IF NOT EXISTS channels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Messages (The Content)
-- Stores text for both DMs and Entry Comments.
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_messages_channel 
        FOREIGN KEY (channel_id) REFERENCES channels(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_messages_sender 
        FOREIGN KEY (sender_id) REFERENCES users(id)
        ON DELETE CASCADE,

    -- Performance Index for retrieving chat history sorted by time
    INDEX idx_channel_created (channel_id, created_at DESC)
) ENGINE=InnoDB;

-- 3. Entries table
-- An entry owns exactly one Channel (Comment Section).
CREATE TABLE IF NOT EXISTS entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    parent_id INT,
    author_id INT NOT NULL,
    channel_id INT UNIQUE NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES entries(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_entries_channel 
        FOREIGN KEY (channel_id) REFERENCES channels(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- Entry Permissions Table
CREATE TABLE IF NOT EXISTS entry_permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    entry_id INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    permission VARCHAR(50),
    FOREIGN KEY (entry_id) REFERENCES entries(id) ON DELETE CASCADE,
    UNIQUE KEY unique_entry_user (entry_id, username)
) ENGINE=InnoDB;

-- 4. Friendships table (bidirectional friend relationships)
-- A friendship owns exactly one Channel (Private DM).
-- Note: 'channel_id' is NULL while status is PENDING, populated when ACCEPTED.
CREATE TABLE IF NOT EXISTS friendships (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id1 INT NOT NULL,
    user_id2 INT NOT NULL,
    channel_id INT UNIQUE NULL,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id1) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id2) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_channel FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE SET NULL,
    UNIQUE KEY unique_friendship (user_id1, user_id2),
    CHECK (user_id1 != user_id2)
) ENGINE=InnoDB;
