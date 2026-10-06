CREATE TABLE owner_appearance (
    owner_username VARCHAR(100) PRIMARY KEY,
    theme_id VARCHAR(64) NOT NULL
);
CREATE TABLE location_appearance (
    tenant_id VARCHAR(56) PRIMARY KEY,
    theme_id VARCHAR(64) NOT NULL
);
