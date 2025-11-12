environment = "stage"
project_name = "ecommerce-microservice"
region = "us-east-1"

docker_registry = "docker.io"
docker_repository = "dantesiio/ecommerce-backend"

replica_count = {
  dev = 1
  stage = 2
  prod = 3
}

resource_limits = {
  dev = {
    cpu    = "500m"
    memory = "512Mi"
  }
  stage = {
    cpu    = "1000m"
    memory = "1Gi"
  }
  prod = {
    cpu    = "2000m"
    memory = "2Gi"
  }
}

enable_monitoring = true
enable_logging = true
enable_tracing = true

