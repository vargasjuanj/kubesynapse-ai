/**
 * Environment configuration for Local Development profile.
 * Points directly to the Spring Boot BFF running on port 8080.
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1'
};
