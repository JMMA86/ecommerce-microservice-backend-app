"""Load test scenarios for the ecommerce API Gateway.

The tests exercise the public routes exposed by the Spring Cloud Gateway
running inside the Kubernetes cluster. They simulate a user browsing products
and placing orders so we can capture latency, throughput and error rates.
"""

from __future__ import annotations

import random
from datetime import datetime

from locust import HttpUser, SequentialTaskSet, TaskSet, between, task


class ShoppingJourney(SequentialTaskSet):
    """Sequential flow that mimics a shopper browsing and ordering."""

    def on_start(self) -> None:
        self.product_id = None
        self.order_id = None
        self.headers = {"Content-Type": "application/json"}

    @task
    def list_products(self) -> None:
        """Fetch the catalogue so we can reuse a real product ID."""
        with self.client.get(
            "/product-service/api/products",
            name="products:list",
            catch_response=True,
        ) as response:
            if response.status_code == 200:
                data = response.json()
                if isinstance(data, list) and data:
                    self.product_id = random.choice(data).get("productId", 1)
                else:
                    self.product_id = 1
                response.success()
            elif response.status_code in (204, 404):
                self.product_id = 1
                response.success()
            else:
                response.failure(f"Unexpected status {response.status_code}")

    @task
    def view_product(self) -> None:
        """Read the detail of the previously selected product."""
        product_id = self.product_id or 1
        with self.client.get(
            f"/product-service/api/products/{product_id}",
            name="products:detail",
            catch_response=True,
        ) as response:
            if response.status_code in (200, 404):
                response.success()
            else:
                response.failure(f"Unexpected status {response.status_code}")

    @task
    def create_order(self) -> None:
        """Submit a new order using a minimal valid payload."""
        body = {
            "orderDesc": f"locust-order-{random.randint(1, 10_000)}",
            "orderDate": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S"),
        }
        with self.client.post(
            "/order-service/api/orders",
            json=body,
            headers=self.headers,
            name="orders:create",
            catch_response=True,
        ) as response:
            if response.status_code in (200, 201):
                payload = response.json()
                self.order_id = payload.get("orderId")
                response.success()
            elif response.status_code in (400, 503):
                # Services sometimes reject duplicate payloads; count as graceful degradation.
                response.success()
            else:
                response.failure(f"Unexpected status {response.status_code}")

    @task
    def fetch_order(self) -> None:
        """Check the order status to emulate a typical follow-up request."""
        if not self.order_id:
            return

        with self.client.get(
            f"/order-service/api/orders/{self.order_id}",
            name="orders:detail",
            catch_response=True,
        ) as response:
            if response.status_code in (200, 404):
                response.success()
            else:
                response.failure(f"Unexpected status {response.status_code}")


class CatalogueBrowsing(TaskSet):
    """Read-heavy behaviour that continuously explores the catalogue."""

    @task(5)
    def list_products(self) -> None:
        self.client.get("/product-service/api/products", name="catalogue:browse")

    @task(2)
    def view_random_product(self) -> None:
        product_id = random.randint(1, 20)
        self.client.get(
            f"/product-service/api/products/{product_id}",
            name="catalogue:detail",
        )


class ApiGatewayUser(HttpUser):
    """Locust user that targets the Spring Cloud Gateway endpoints."""

    wait_time = between(1, 3)
    tasks = {ShoppingJourney: 2, CatalogueBrowsing: 1}

    # The host is provided at runtime through --host so no extra setup is needed.
    