# Spring Boot DevOps CI/CD Project

A trainer-ready, end-to-end project that builds a Spring Boot REST API and deploys it to Kubernetes.

## Toolchain used

**Four core DevOps tools:** Git/GitHub, Jenkins, Docker, Kubernetes.  
**Build tool:** Maven. Java 17 is used for the application.

## Application

A small Student API:
- `GET /api/students` – list sample students
- `GET /api/students/{id}` – fetch one student
- `GET /actuator/health` – health endpoint

## Repository layout

```text
springboot-devops-k8s-live-project/
├── src/main/java/com/trainerstack/studentapi/
│   ├── StudentApiApplication.java
│   ├── controller/StudentController.java
│   └── model/Student.java
├── src/main/resources/application.properties
├── src/test/java/com/trainerstack/studentapi/StudentControllerTest.java
├── Dockerfile
├── .dockerignore
├── .gitignore
├── pom.xml
├── Jenkinsfile
└── k8s/
    ├── deployment.yaml
    └── service.yaml
```

## Run locally

Requirements: Java 17 and Maven 3.8+.

```bash
mvn clean test
mvn spring-boot:run
```

Open `http://localhost:8080/api/students` and `http://localhost:8080/actuator/health`.

## Build and run with Docker

```bash
docker build -t springboot-student-api:local .
docker run --rm -p 8080:8080 springboot-student-api:local
```

## Deploy to Kubernetes manually

First build and push your image to Docker Hub:

```bash
docker login
docker build -t YOUR_DOCKERHUB_USERNAME/springboot-student-api:1.0 .
docker push YOUR_DOCKERHUB_USERNAME/springboot-student-api:1.0
```

Update `k8s/deployment.yaml` with your image name, then:

```bash
kubectl apply -f k8s/
kubectl get deployments,pods,services
kubectl rollout status deployment/student-api
```

For Minikube:

```bash
minikube service student-api
```

The Service is NodePort `30080`. In managed Kubernetes, use a LoadBalancer or Ingress for a real external endpoint.

## Jenkins pipeline setup

Create these Jenkins credentials:

1. `dockerhub-creds` – Username with password/token (Username with password credential type).
2. `kubeconfig-file` – Secret file containing the kubeconfig for the target cluster.

Configure Jenkins job as **Pipeline** and point it to this GitHub repository. Set the following job parameters/environment values as needed:
- `DOCKERHUB_USERNAME` – Docker Hub account name.
- `IMAGE_NAME` – `springboot-student-api`.
- `K8S_NAMESPACE` – `default` (or your teaching namespace).

The Jenkins agent must have Java 17, Maven, Docker CLI/daemon access, and kubectl installed. It must be able to reach Docker Hub and the Kubernetes API endpoint.

Configure a GitHub webhook to trigger Jenkins on push, or use **Build Now** for the first classroom run.

## Important trainer note

The Jenkinsfile uses a Docker Hub image tag based on the Git commit SHA. It applies the Kubernetes manifests, substitutes the image name, and waits for rollout completion. Do not store Docker passwords or kubeconfig content in GitHub or in the repository.
