describe("Shipping Service E2E Tests", () => {
  const baseUrl = `${Cypress.config('baseUrl')}/shipping-service`;
  let createdOrderId = 1;
  let createdProductId = 1;

  it("1. Should create a new order item", () => {
    const orderItemData = {
      productId: createdProductId,
      orderId: createdOrderId,
      orderedQuantity: 2,
      productDto: { productId: createdProductId },
      orderDto: { orderId: createdOrderId },
    };

    cy.request("POST", `${baseUrl}/api/shippings`, orderItemData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.productId).to.eq(createdProductId);
        expect(response.body.orderId).to.eq(createdOrderId);
        expect(response.body.orderedQuantity).to.eq(2);
      }
    );
  });

  it("2. Should get the created order item by ID", () => {
    cy.request(
      "GET",
      `${baseUrl}/api/shippings/${createdOrderId}/${createdProductId}`
    ).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.productId).to.eq(createdProductId);
      expect(response.body.orderId).to.eq(createdOrderId);
      expect(response.body.orderedQuantity).to.eq(2);
    });
  });

  it("3. Should update the order item", () => {
    const updatedData = {
      productId: createdProductId,
      orderId: createdOrderId,
      orderedQuantity: 5,
      productDto: { productId: createdProductId },
      orderDto: { orderId: createdOrderId },
    };

    cy.request("PUT", `${baseUrl}/api/shippings`, updatedData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.orderedQuantity).to.eq(5);
      }
    );
  });

  it("4. Should get all order items", () => {
    cy.request("GET", `${baseUrl}/api/shippings`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("collection");
      expect(response.body.collection).to.be.an("array");
      expect(response.body.collection.length).to.be.greaterThan(0);
    });
  });

  it("5. Should delete the order item", () => {
    cy.request(
      "DELETE",
      `${baseUrl}/api/shippings/${createdOrderId}/${createdProductId}`
    ).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.eq(true);
    });
  });
});
