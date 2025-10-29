describe("Service Discovery E2E Tests", () => {
  const baseUrl = "http://localhost:8761";

  it("1. Should access Eureka dashboard", () => {
    cy.request(baseUrl).then((response) => {
      expect(response.status).to.eq(200);
      // Eureka dashboard returns HTML, so just check it's accessible
    });
  });

  it("2. Should get Eureka applications", () => {
    cy.request(`${baseUrl}/eureka/apps`).then((response) => {
      expect(response.status).to.eq(200);
      // Check that it's XML or JSON response from Eureka
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
      // Assuming some services are registered
      expect(response.body).to.not.be.empty;
    });
  });

  it("5. Should access Eureka info endpoint", () => {
    cy.request(`${baseUrl}/actuator/info`).then((response) => {
      expect(response.status).to.eq(200);
    });
  });
});
