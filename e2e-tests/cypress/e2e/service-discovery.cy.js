describe("Service Discovery E2E Tests", () => {
  const baseUrl = process.env.CYPRESS_EUREKA_URL || "http://localhost:8761";

  it("1. Should access Eureka dashboard", () => {
    cy.request(baseUrl).then((response) => {
      expect(response.status).to.eq(200);
    });
  });

  it("2. Should get Eureka applications", () => {
    cy.request(`${baseUrl}/eureka/apps`).then((response) => {
      expect(response.status).to.eq(200);
    });
  });

  it("3. Should check service discovery health", () => {
    cy.request(`${baseUrl}/actuator/health`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.status).to.eq("UP");
    });
  });

  it("4. Should verify registered services", () => {
    cy.request(`${baseUrl}/eureka/apps`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.not.be.empty;
    });
  });

  it("5. Should access Eureka info endpoint", () => {
    cy.request(`${baseUrl}/actuator/info`).then((response) => {
      expect(response.status).to.eq(200);
    });
  });
});
