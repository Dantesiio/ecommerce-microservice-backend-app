"""
Locust performance suite covering key commerce flows exposed through the proxy-client.

Scenarios implemented:
- High concurrency catalog browsing
- Repeated product detail lookups simulating search queries
- Concurrent order creation and downstream payment/shipping execution
- Full purchase flow (browse -> order -> pay -> ship)
- Mixed CRUD execution across orders and favourites

Default target host is expected to be the proxy-client gateway (context path `/app`).
Override host/context via standard Locust CLI flags or environment variables.
"""
import logging
import os
import random
import time
from typing import Any, Dict, List, Optional
from urllib.parse import quote

from locust import HttpUser, SequentialTaskSet, between, events, task

LOGGER = logging.getLogger(__name__)

DEFAULT_CONTEXT_PATH = os.getenv("API_CONTEXT_PATH", "/app").rstrip("/") or "/app"
CATALOG_TTL_SECONDS = int(os.getenv("CATALOG_CACHE_TTL", "30"))
STATIC_CART_ID = int(os.getenv("PERF_CART_ID", "77"))
STATIC_USER_ID = int(os.getenv("PERF_USER_ID", "101"))
FAVOURITE_PRODUCT_IDS = [
    int(pid.strip())
    for pid in os.getenv("PERF_PRODUCT_IDS", "501,502,503").split(",")
    if pid.strip().isdigit()
]

# Valores de ejemplo para ejecutar la suite fuera de Docker sin exponer credenciales reales
DEFAULT_USERNAME = "perf.user"
DEFAULT_PASSWORD = "PerfUserPass123!"

AUTH_ENDPOINT = "/api/authenticate"
AUTH_USERNAME = os.getenv("PERF_USERNAME", DEFAULT_USERNAME)
AUTH_PASSWORD = os.getenv("PERF_PASSWORD", DEFAULT_PASSWORD)

PRODUCTS_ENDPOINT = "/api/products"
ORDERS_ENDPOINT = "/api/orders"
PAYMENTS_ENDPOINT = "/api/payments"
SHIPPINGS_ENDPOINT = "/api/shippings"
FAVOURITES_ENDPOINT = "/api/favourites"


def _extract_collection(payload: Any) -> List[Dict[str, Any]]:
    """Attempt to normalize collection responses coming from different services."""
    if isinstance(payload, dict):
        if "collection" in payload and isinstance(payload["collection"], list):
            return payload["collection"]
        # some responses use camelCase
        if "items" in payload and isinstance(payload["items"], list):
            return payload["items"]
    if isinstance(payload, list):
        return payload
    return []


class CommerceUser(HttpUser):
    wait_time = between(1, 3)
    tasks = []  # populated after task set definitions

    context_path = DEFAULT_CONTEXT_PATH

    def on_start(self) -> None:
        self._catalog_cache: List[Dict[str, Any]] = []
        self._catalog_cached_at: float = 0.0
        self.auth_headers: Dict[str, str] = {}
        self._missing_auth_logged: bool = False

        self.authenticate()

    def get_catalog_snapshot(self) -> List[Dict[str, Any]]:
        now = time.time()
        if (now - self._catalog_cached_at) > CATALOG_TTL_SECONDS or not self._catalog_cache:
            response = self.client.get(
                f"{self.context_path}{PRODUCTS_ENDPOINT}",
                name="catalog:list",
            )
            if response.ok:
                payload = response.json()
                self._catalog_cache = _extract_collection(payload)
                self._catalog_cached_at = now
            else:
                LOGGER.warning("catalog:list failed with status %s", response.status_code)
        return self._catalog_cache

    def pick_product(self) -> Optional[Dict[str, Any]]:
        catalog = self.get_catalog_snapshot()
        if catalog:
            return random.choice(catalog)
        if FAVOURITE_PRODUCT_IDS:
            return {"productId": random.choice(FAVOURITE_PRODUCT_IDS)}
        return None

    def pick_product_id(self) -> Optional[int]:
        product = self.pick_product()
        if product is None:
            return None
        for key in ("productId", "product_id", "id"):
            value = product.get(key) if isinstance(product, dict) else None
            if isinstance(value, int):
                return value
            if isinstance(value, str) and value.isdigit():
                return int(value)
        return None

    def build_url(self, path: str) -> str:
        if path.startswith("/"):
            return f"{self.context_path}{path}"
        return f"{self.context_path}/{path}"

    def authenticate(self) -> None:
        response = self.client.post(
            self.build_url(AUTH_ENDPOINT),
            json={"username": AUTH_USERNAME, "password": AUTH_PASSWORD},
            name="auth:login",
        )
        if response.status_code == 401:
            LOGGER.warning("auth:login rechazó las credenciales configuradas; se omiten flujos seguros")
            self.auth_headers = {}
            return

        if not response.ok:
            LOGGER.warning("auth:login falló con estado %s", response.status_code)
            return

        payload = response.json()
        token = payload.get("jwtToken") if isinstance(payload, dict) else None
        if not token:
            LOGGER.warning("auth:login no devolvió jwtToken")
            return

        self.auth_headers = {"Authorization": f"Bearer {token}"}
        LOGGER.info("Token JWT obtenido correctamente para Locust")

    def require_auth_headers(self) -> Optional[Dict[str, str]]:
        if self.auth_headers:
            return self.auth_headers
        if not self._missing_auth_logged:
            LOGGER.warning(
                "Sin token JWT disponible; se omiten las peticiones a endpoints protegidos"
            )
            self._missing_auth_logged = True
        return None


class BrowseCatalog(SequentialTaskSet):

    @task
    def browse_catalog(self) -> None:
        snapshot = self.user.get_catalog_snapshot()
        if snapshot:
            product = random.choice(snapshot)
            product_id = product.get("productId")
            if product_id is not None:
                self.client.get(
                    self.user.build_url(f"{PRODUCTS_ENDPOINT}/{product_id}"),
                    name="catalog:detail",
                )
        self.wait()


class ProductSearches(SequentialTaskSet):

    @task
    def search_products(self) -> None:
        product_id = self.user.pick_product_id()
        if product_id is not None:
            params = {"search": product_id}
            # Even if backend ignores the query param, it generates unique metric entries per search.
            self.client.get(
                self.user.build_url(f"{PRODUCTS_ENDPOINT}/{product_id}"),
                params=params,
                name="catalog:search-detail",
            )
        else:
            self.client.get(
                    self.user.build_url(PRODUCTS_ENDPOINT),
                name="catalog:search-fallback",
            )
        self.wait()


class OrderCreation(SequentialTaskSet):

    @task
    def create_order(self) -> None:
        product_id = self.user.pick_product_id()
        order_fee = round(random.uniform(50.0, 250.0), 2)
        payload = {
            "orderDesc": f"Performance order {int(time.time())}",
            "orderFee": order_fee,
            "cart": {"cartId": STATIC_CART_ID},
        }
        headers = self.user.require_auth_headers()
        if headers is None:
            self.wait()
            return
        response = self.client.post(
            self.user.build_url(ORDERS_ENDPOINT),
            json=payload,
            headers=headers,
            name="orders:create",
        )
        if response.ok:
            order_id = response.json().get("orderId")
            if order_id is not None:
                payment_payload = {
                    "order": {"orderId": order_id},
                    "paymentStatus": "COMPLETED",
                }
                self.client.post(
                    self.user.build_url(PAYMENTS_ENDPOINT),
                    json=payment_payload,
                    headers=headers,
                    name="payments:create",
                )
                if product_id is not None:
                    shipping_payload = {
                        "order": {"orderId": order_id},
                        "product": {"productId": product_id},
                        "orderedQuantity": random.randint(1, 3),
                    }
                    self.client.post(
                        self.user.build_url(SHIPPINGS_ENDPOINT),
                        json=shipping_payload,
                        headers=headers,
                        name="shippings:create",
                    )
        self.wait()


class CheckoutFlow(SequentialTaskSet):

    @task
    def full_checkout(self) -> None:
        product_id = self.user.pick_product_id()
        if product_id is None:
            LOGGER.warning("No product id available for checkout flow")
            self.wait()
            return

        headers = self.user.require_auth_headers()
        if headers is None:
            self.wait()
            return

        # Step 1: consult product
        self.client.get(
            self.user.build_url(f"{PRODUCTS_ENDPOINT}/{product_id}"),
            headers=headers,
            name="flow:product-detail",
        )

        # Step 2: create order
        payload = {
            "orderDesc": "Checkout flow order",
            "orderFee": round(random.uniform(80.0, 180.0), 2),
            "cart": {"cartId": STATIC_CART_ID},
        }
        response = self.client.post(
            self.user.build_url(ORDERS_ENDPOINT),
            json=payload,
            headers=headers,
            name="flow:create-order",
        )
        if not response.ok:
            self.wait()
            return

        order_body = response.json()
        order_id = order_body.get("orderId")
        if order_id is None:
            self.wait()
            return

        # Step 3: pay order
        payment_payload = {
            "order": {"orderId": order_id},
            "paymentStatus": random.choice(["PENDING", "COMPLETED"]),
        }
        self.client.post(
            self.user.build_url(PAYMENTS_ENDPOINT),
            json=payment_payload,
            headers=headers,
            name="flow:pay-order",
        )

        # Step 4: ship order
        shipping_payload = {
            "order": {"orderId": order_id},
            "product": {"productId": product_id},
            "orderedQuantity": 1,
        }
        self.client.post(
            self.user.build_url(SHIPPINGS_ENDPOINT),
            json=shipping_payload,
            headers=headers,
            name="flow:ship-order",
        )
        self.wait()


class MixedCrud(SequentialTaskSet):

    @task
    def mixed_operations(self) -> None:
        headers = self.user.require_auth_headers()
        if headers is None:
            self.wait()
            return

        self.client.get(
            self.user.build_url(ORDERS_ENDPOINT),
            headers=headers,
            name="crud:list-orders",
        )

        order_id = random.randint(6000, 9000)
        update_payload = {
            "orderDesc": "Load test update",
            "orderFee": round(random.uniform(90.0, 220.0), 2),
            "cart": {"cartId": STATIC_CART_ID},
        }
        self.client.put(
            self.user.build_url(f"{ORDERS_ENDPOINT}/{order_id}"),
            json=update_payload,
            headers=headers,
            name="crud:update-order",
        )

        favourite_payload = {
            "userId": STATIC_USER_ID,
            "productId": random.choice(FAVOURITE_PRODUCT_IDS) if FAVOURITE_PRODUCT_IDS else 501,
            "likeDate": time.strftime("%d-%m-%Y__%H:%M:%S:000000"),
        }
        favourite_resp = self.client.post(
            self.user.build_url(FAVOURITES_ENDPOINT),
            json=favourite_payload,
            headers=headers,
            name="crud:create-favourite",
        )
        if favourite_resp.ok:
            favourite_product_id = favourite_payload["productId"]
            like_date_encoded = quote(favourite_payload["likeDate"], safe="")
            self.client.delete(
                self.user.build_url(
                    f"{FAVOURITES_ENDPOINT}/{STATIC_USER_ID}/{favourite_product_id}/{like_date_encoded}"
                ),
                headers=headers,
                name="crud:delete-favourite",
            )
        self.wait()


CommerceUser.tasks = {
    BrowseCatalog: 3,
    ProductSearches: 2,
    OrderCreation: 2,
    CheckoutFlow: 2,
    MixedCrud: 1,
}


@events.test_stop.add_listener
def log_test_summary(environment, **kwargs) -> None:
    stats = environment.stats.total
    if stats.num_requests == 0:
        LOGGER.info("No requests executed during test run")
        return

    LOGGER.info(
        "Performance summary | requests=%s | fail_ratio=%.2f%% | rps=%.2f | p50=%dms | p95=%dms | p99=%dms",
        stats.num_requests,
        stats.fail_ratio * 100,
        environment.stats.total.current_rps,
        stats.get_response_time_percentile(0.50),
        stats.get_response_time_percentile(0.95),
        stats.get_response_time_percentile(0.99),
    )