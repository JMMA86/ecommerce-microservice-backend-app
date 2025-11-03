describe("Order Service E2E Tests", () => {
  const baseUrl = `${Cypress.config('baseUrl')}/order-service`;
  let createdCartId;
  let createdOrderId;

  it("1. Should create a new cart", () => {
    const cartData = {
      userId: 1,
    };

    cy.request("POST", `${baseUrl}/api/carts`, cartData).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("cartId");
      expect(response.body.userId).to.eq(cartData.userId);
      createdCartId = response.body.cartId;
    });
  });

  it("2. Should create a new order", () => {
    const orderData = {
      orderDate: "2023-10-29T10:00:00",
      orderDesc: "Test order",
      orderFee: 100.0,
      cartDto: { cartId: createdCartId },
    };

    cy.request("POST", `${baseUrl}/api/orders`, orderData).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("orderId");
      expect(response.body.orderDesc).to.eq(orderData.orderDesc);
      expect(response.body.orderFee).to.eq(orderData.orderFee);
      createdOrderId = response.body.orderId;
    });
  });

  it("3. Should get the created order by ID", () => {
    cy.request("GET", `${baseUrl}/api/orders/${createdOrderId}`).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.orderId).to.eq(createdOrderId);
        expect(response.body.orderDesc).to.eq("Test order");
      }
    );
  });

  it("4. Should update the order", () => {
    const updatedData = {
      orderId: createdOrderId,
      orderDate: "2023-10-29T10:00:00",
      orderDesc: "Updated test order",
      orderFee: 150.0,
      cartDto: { cartId: createdCartId },
    };

    cy.request(
      "PUT",
      `${baseUrl}/api/orders/${createdOrderId}`,
      updatedData
    ).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.orderDesc).to.eq("Updated test order");
      expect(response.body.orderFee).to.eq(150.0);
    });
  });

  it("5. Should get all orders", () => {
    cy.request("GET", `${baseUrl}/api/orders`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("collection");
      expect(response.body.collection).to.be.an("array");
      expect(response.body.collection.length).to.be.greaterThan(0);
    });
  });
});
