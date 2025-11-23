# SonarQube Setup

## Configuración

### SonarCloud
- URL: https://sonarcloud.io/
- Organization: [tu organization]
- Project Key: ecommerce-microservice-backend

### Quality Gates
- Coverage mínimo: 80%
- Duplicación máxima: 3%
- Code Smells: A rating
- Security: A rating

## Integración en Pipeline

Archivo: `.github/workflows/sonarqube-analysis.yml`

```yaml
name: SonarQube Analysis

on:
  push:
    branches: [develop, master]
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarqube:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0

      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Cache SonarCloud packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      - name: Build and analyze
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          ./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
            -Dsonar.projectKey=ecommerce-microservice-backend \
            -Dsonar.organization=${{ secrets.SONAR_ORGANIZATION }} \
            -Dsonar.host.url=https://sonarcloud.io
```