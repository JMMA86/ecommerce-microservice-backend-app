describe("User Service E2E Tests", () => {
  const baseUrl = `${Cypress.config('baseUrl')}/user-service/api/users`;
  let createdUserId;

  it("1. Should create a new user", () => {
    const userData = {
      firstName: "John",
      lastName: "Doe",
      imageUrl: "http://example.com/image.jpg",
      email: "john.doe@example.com",
      phone: "+1234567890",
    };

    cy.request("POST", baseUrl, userData).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("userId");
      expect(response.body.firstName).to.eq(userData.firstName);
      expect(response.body.lastName).to.eq(userData.lastName);
      expect(response.body.email).to.eq(userData.email);
      createdUserId = response.body.userId;
    });
  });

  it("2. Should get the created user by ID", () => {
    cy.request("GET", `${baseUrl}/${createdUserId}`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.userId).to.eq(createdUserId);
      expect(response.body.firstName).to.eq("John");
      expect(response.body.lastName).to.eq("Doe");
    });
  });

  it("3. Should update the user", () => {
    const updatedData = {
      userId: createdUserId,
      firstName: "Jane",
      lastName: "Smith",
      imageUrl: "http://example.com/newimage.jpg",
      email: "jane.smith@example.com",
      phone: "+0987654321",
    };

    cy.request("PUT", `${baseUrl}/${createdUserId}`, updatedData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.firstName).to.eq("Jane");
        expect(response.body.lastName).to.eq("Smith");
        expect(response.body.email).to.eq("jane.smith@example.com");
      }
    );
  });

  it("4. Should get all users", () => {
    cy.request("GET", baseUrl).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("collection");
      expect(response.body.collection).to.be.an("array");
      expect(response.body.collection.length).to.be.greaterThan(0);
    });
  });

  it("5. Should delete the user", () => {
    cy.request("DELETE", `${baseUrl}/${createdUserId}`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.eq(true);
    });
  });
});
