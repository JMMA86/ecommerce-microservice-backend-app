# Taller 2: Pruebas y lanzamiento

- Nombre: Juan Manuel Marín Angarita
- Código: A00382037

## Resumen de avance
- Se configuró un pipeline de Jenkins ejecutándose sobre un agente Kubernetes con contenedores dedicados para Maven, Docker, kubectl y Node, habilitando el build y despliegue de los microservicios seleccionados.
- Se habilitaron etapas de construcción, pruebas unitarias, integración y E2E para los servicios `favourite-service`, `order-service`, `payment-service`, `product-service`, `service-discovery`, `shipping-service` y `user-service`.
- El pipeline despliega los artefactos en el clúster Kubernetes del entorno `ecommerce-dev` mediante Helm y verifica el estado de los despliegues antes de ejecutar las pruebas end-to-end.
- Se habilitó la ejecución opcional de pruebas de rendimiento con Locust para medir latencia y throughput desde el API Gateway.

## Configuración de la plataforma
- **Jenkins**: se definió un `Jenkinsfile` con agente Kubernetes y un pod template que incluye los contenedores requeridos (Maven 3.9.6, Docker DinD, Helm/kubectl, Node 20).
- **Docker Registry**: el pipeline parametriza el registro objetivo a través de la variable de entorno `DOCKER_REGISTRY` para la fase de build y push de imágenes.
- **Kubernetes & Helm**: Helm Chart `helm-charts/ecommerce` ajustado para templating correcto del namespace y despliegue de los microservicios; se crea/elimina el namespace `ecommerce-dev` según la ejecución del pipeline.
- **Gestión de secretos/config**: las variables de configuración se resuelven desde ConfigMaps compartidos (`microservices-config`) referenciados en los despliegues.

## Configuración de Jenkins
Se instalaron los plugins necesarios para la configuración del sistema y el pipeline (principalmente Git, Pipeline, Kubernetes y Stage View).

Primero se crearon las credenciales necesarias para la conexión de Jenkins con Minikube y los demás servicios:
![alt text](images/image.png)

Estas credenciales se utilizaron en la creación de un Cloud de minikube (kubernetes) con la configuración necesaria de los enlaces:
![alt text](images/image-1.png)
![alt text](images/image-2.png)

Después se procedió a crear el pipeline con la configuración del repositorio y las ramas involucradas (`dev` y `master`):
![alt text](images/image-3.png)

Además de otros parámetros que sirven para configurar el inicio de cada pipeline (configurados en el `Jenkinsfile`):


## Pipeline CI (Dev Environment)
- **Configuración**: etapas `Checkout`, `Build with Maven` y `Run Unitary Tests`; se construyen sucesivamente los siete microservicios seleccionados ejecutando `mvn clean package` y `mvn test` en cada módulo.
- **Resultado**: ejecución exitosa que produce artefactos listos para pruebas posteriores y valida que las pruebas unitarias personalizadas pasen. Se realizaron un total de:

- 6 pruebas para FavouriteService
- 7 pruebas para OrderService
- 6 pruebas para PaymentService
- 14 pruebas para ProductService
- 6 pruebas para OrderItemService
- 24 pruebas para UserService

### Análisis
- Las pruebas unitarias existentes se ejecutaron correctamente; se planea añadir al menos cinco pruebas unitarias adicionales por servicio para cubrir componentes críticos (repositorios, servicios y controladores REST) conforme al requerimiento.
- Cobertura de código actual capturada via informes de Maven Surefire; se documentará la métrica exacta cuando se integren los reportes en Jenkins.

## Pipeline QA (Stage Environment)
- **Configuración**: reutiliza el mismo `Jenkinsfile` con etapa `Run Integration Tests`, invocando `mvn verify -Pintegration-tests` en cada microservicio para validar la comunicación entre servicios.
- **Resultado**: se ejecutan pruebas de integración existentes; se agregará un conjunto de al menos cinco pruebas adicionales que cubran interacciones entre microservicios (e.g., pedidos → pagos → envíos) para cumplir la rúbrica.

_(Insertar images/imagen de la etapa Run Integration Tests)_

### Análisis
- Actualmente las suites de integración confirman la correcta resolución de dependencias entre servicios a través de Eureka/Config Server.
- Se planifica instrumentar pruebas más profundas que validen consistencia y resiliencia entre servicios dependientes.

## Pipeline Master (Producción Controlada)
- **Configuración**: etapas `Deploy to Kubernetes`, `Wait for Services`, `Resolve API Gateway Endpoint` y `Run E2E Tests`.
- **Resultado**: Helm despliega los servicios en `ecommerce-dev`, valida la disponibilidad de pods y servicios, resuelve la IP externa del API Gateway y ejecuta pruebas E2E basadas en Cypress contra el entorno real.

_(Insertar images/imagen del dashboard del pipeline master con todas las etapas verdes)_

### Análisis
- Las pruebas E2E (Cypress) confirman flujos de usuario completos a través del API Gateway; actualmente cubren login de usuario, creación de pedidos, pagos y seguimiento de envíos.
- Las ejecuciones de Locust generan reportes HTML y CSV con latencias promedio, percentiles y tasa de errores para evaluar salud del entorno.
- Monitoreo adicional planificado: integraciones con Prometheus/Grafana para validar KPIs en ejecuciones subsecuentes.

## Pruebas implementadas
- **Unitarias**: suites existentes ejecutadas mediante Maven; pendiente documentar las cinco nuevas pruebas por microservicio (en desarrollo).
- **Integración**: ejecutadas a través del perfil `integration-tests`; en progreso la adición de cinco casos que cubran escenarios multi-servicio.
- **E2E**: colección Cypress (`e2e-tests/cypress/e2e/*.cy.js`) integrada al pipeline; los scripts validan favoritos, pedidos, pagos, productos, servicio de descubrimiento, envíos y usuarios.
- **Rendimiento (Locust)**: escenario `tests/performance/locustfile.py` que simula navegación de catálogo y creación de órdenes contra el API Gateway.

## Métricas y análisis de resultados
- Jenkins Centraliza los logs de cada etapa y registra el estado final de los deployments mediante `kubectl get deployments` y `kubectl get pods`.
- Las métricas de tiempo de respuesta y throughput se consultan en los reportes de Locust almacenados en `tests/performance/reports`.
- Se documentarán tasas de errores a partir de los resultados de Cypress y los reportes de Maven Surefire/FailSafe.

_(Insertar images/imagen de métricas de pruebas E2E)_

## Próximos pasos
- Completar la suite de pruebas unitarias e integración adicionales requeridas.
- Definir umbrales aceptables para las métricas de Locust (latencia p95, throughput, tasa de errores) e incorporarlos al criterio de aprobación del pipeline.
- Automatizar la generación de Release Notes al cierre de cada ejecución del pipeline Master.
- Incorporar publicación de imágenes Docker firmadas y versionadas en el registro configurado.
- Documentar con capturas y adjuntar un ZIP con las pruebas implementadas conforme a los requerimientos del taller.

_(Insertar images/imagen del roadmap o tablero de seguimiento)_
