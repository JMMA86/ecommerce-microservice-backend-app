describe("Product Service E2E Tests", () => {
  const baseUrl = "http://localhost:8500/product-service";
  let createdCategoryId;
  let createdProductId;

  it("1. Should create a new category", () => {
    const categoryData = {
      categoryTitle: "Electronics",
      imageUrl: "http://example.com/electronics.jpg",
    };

    cy.request("POST", `${baseUrl}/api/categories`, categoryData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body).to.have.property("categoryId");
        expect(response.body.categoryTitle).to.eq(categoryData.categoryTitle);
        createdCategoryId = response.body.categoryId;
      }
    );
  });

  it("2. Should create a new product", () => {
    const productData = {
      productTitle: "Laptop",
      imageUrl: "http://example.com/laptop.jpg",
      sku: "LAP123",
      priceUnit: 999.99,
      quantity: 10,
      categoryDto: { categoryId: createdCategoryId },
    };

    cy.request("POST", `${baseUrl}/api/products`, productData).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body).to.have.property("productId");
        expect(response.body.productTitle).to.eq(productData.productTitle);
        expect(response.body.sku).to.eq(productData.sku);
        createdProductId = response.body.productId;
      }
    );
  });

  it("3. Should get the created product by ID", () => {
    cy.request("GET", `${baseUrl}/api/products/${createdProductId}`).then(
      (response) => {
        expect(response.status).to.eq(200);
        expect(response.body.productId).to.eq(createdProductId);
        expect(response.body.productTitle).to.eq("Laptop");
      }
    );
  });

  it("4. Should update the product", () => {
    const updatedData = {
      productId: createdProductId,
      productTitle: "Gaming Laptop",
      imageUrl: "http://example.com/gaming-laptop.jpg",
      sku: "GLAP123",
      priceUnit: 1299.99,
      quantity: 5,
      categoryDto: { categoryId: createdCategoryId },
    };

    cy.request(
      "PUT",
      `${baseUrl}/api/products/${createdProductId}`,
      updatedData
    ).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.productTitle).to.eq("Gaming Laptop");
      expect(response.body.priceUnit).to.eq(1299.99);
    });
  });

  it("5. Should get all products", () => {
    cy.request("GET", `${baseUrl}/api/products`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("collection");
      expect(response.body.collection).to.be.an("array");
      expect(response.body.collection.length).to.be.greaterThan(0);
    });
  });
});
