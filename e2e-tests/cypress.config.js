const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL || 'http://localhost:8300',
    // Disable baseUrl verification in CI/CD environments
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
  // Increase timeouts for Kubernetes environment
  defaultCommandTimeout: 10000,
  requestTimeout: 10000,
  responseTimeout: 10000,
});
