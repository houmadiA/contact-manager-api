-- Contact manager schema.
--
-- Owned by the persistence adapter: the domain has no opinion about tables, only about the
-- ContactRepository / UserRepository / RefreshTokenStore ports these support.

CREATE TABLE contacts (
    id           UUID         PRIMARY KEY,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(254) NOT NULL,
    phone_number VARCHAR(40),
    company      VARCHAR(150),
    job_title    VARCHAR(150),
    address      VARCHAR(255),
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL
);

-- Email is the business key: the domain enforces uniqueness, the database guarantees it.
CREATE UNIQUE INDEX ux_contacts_email ON contacts (email);

-- Supports the default listing order and the free-text search, which filters on lower(...).
CREATE INDEX ix_contacts_last_name ON contacts (lower(last_name), lower(first_name));
CREATE INDEX ix_contacts_company ON contacts (lower(company));

CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_users_username ON users (lower(username));

CREATE TABLE user_roles (
    user_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Refresh tokens are stored hashed, exactly like passwords: a leaked database backup must not
-- hand out live sessions. Logout and rotation delete the row.
CREATE TABLE refresh_tokens (
    id         UUID        PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);
