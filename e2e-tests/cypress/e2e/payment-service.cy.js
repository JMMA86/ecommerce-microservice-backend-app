describe("Payment Service E2E Tests", () => {
  const baseUrl = "http://localhost:8400/payment-service";
  let createdPaymentId;

  it("1. Should create a new payment", () => {
    const paymentData = {
      isPayed: false,
      paymentStatus: "PENDING",
      orderDto: { orderId: 1 }, // Assuming order exists
    };

    cy.request("POST", `${baseUrl}/api/payments`, paymentData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body).to.have.property("paymentId");
        expect(response.body.isPayed).to.eq(paymentData.isPayed);
        expect(response.body.paymentStatus).to.eq(paymentData.paymentStatus);
        createdPaymentId = response.body.paymentId;
      }
    );
  });

  it("2. Should get the created payment by ID", () => {
    cy.request("GET", `${baseUrl}/api/payments/${createdPaymentId}`).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.paymentId).to.eq(createdPaymentId);
        expect(response.body.isPayed).to.eq(false);
      }
    );
  });

  it("3. Should update the payment", () => {
    const updatedData = {
      paymentId: createdPaymentId,
      isPayed: true,
      paymentStatus: "COMPLETED",
      orderDto: { orderId: 1 },
    };

    cy.request("PUT", `${baseUrl}/api/payments`, updatedData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.isPayed).to.eq(true);
        expect(response.body.paymentStatus).to.eq("COMPLETED");
      }
    );
  });

  it("4. Should get all payments", () => {
    cy.request("GET", `${baseUrl}/api/payments`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("collection");
      expect(response.body.collection).to.be.an("array");
      expect(response.body.collection.length).to.be.greaterThan(0);
    });
  });

  it("5. Should delete the payment", () => {
    cy.request("DELETE", `${baseUrl}/api/payments/${createdPaymentId}`).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body).to.eq(true);
      }
    );
  });
});
