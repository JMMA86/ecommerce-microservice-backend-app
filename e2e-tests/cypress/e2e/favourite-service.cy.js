describe("Favourite Service E2E Tests", () => {
  const baseUrl = `${Cypress.config('baseUrl')}/favourite-service`;
  let createdUserId = 1;
  let createdProductId = 1;
  let likeDate = "2023-10-29T10:00:00";

  it("1. Should create a new favourite", () => {
    const favouriteData = {
      userId: createdUserId,
      productId: createdProductId,
      likeDate: likeDate,
      userDto: { userId: createdUserId },
      productDto: { productId: createdProductId },
    };

    cy.request("POST", `${baseUrl}/api/favourites`, favouriteData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.userId).to.eq(createdUserId);
        expect(response.body.productId).to.eq(createdProductId);
        expect(response.body.likeDate).to.include(likeDate.split("T")[0]);
      }
    );
  });

  it("2. Should get the created favourite by ID", () => {
    cy.request(
      "GET",
      `${baseUrl}/api/favourites/${createdUserId}/${createdProductId}/${encodeURIComponent(
        likeDate
      )}`
    ).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.userId).to.eq(createdUserId);
      expect(response.body.productId).to.eq(createdProductId);
    });
  });

  it("3. Should update the favourite", () => {
    const updatedLikeDate = "2023-10-29T11:00:00";
    const updatedData = {
      userId: createdUserId,
      productId: createdProductId,
      likeDate: updatedLikeDate,
      userDto: { userId: createdUserId },
      productDto: { productId: createdProductId },
    };

    cy.request("PUT", `${baseUrl}/api/favourites`, updatedData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.likeDate).to.include(
          updatedLikeDate.split("T")[0]
        );
      }
    );
  });

  it("4. Should get all favourites", () => {
    cy.request("GET", `${baseUrl}/api/favourites`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("collection");
      expect(response.body.collection).to.be.an("array");
      expect(response.body.collection.length).to.be.greaterThan(0);
    });
  });

  it("5. Should delete the favourite", () => {
    cy.request(
      "DELETE",
      `${baseUrl}/api/favourites/${createdUserId}/${createdProductId}/${encodeURIComponent(
        likeDate
      )}`
    ).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.eq(true);
    });
  });
});
