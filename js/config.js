/**
 * FixMate Frontend Configuration
 * Central configuration file for API endpoints and application defaults.
 */
const CONFIG = {
    API_BASE_URL: "http://localhost:8085/api",
    TOKEN_KEY: "fixmate_token",
    USER_KEY: "fixmate_user",
    THEME_KEY: "fixmate_theme"
};

// Freeze configuration to prevent accidental modification
Object.freeze(CONFIG);
