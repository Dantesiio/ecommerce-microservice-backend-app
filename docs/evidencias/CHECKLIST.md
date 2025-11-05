# 📸 Checklist de Evidencias Pendientes

## GitHub Actions (Prioritario)

- [ ] Hacer un push para trigger workflow
- [ ] Esperar a que workflow termine (5-10 min)
- [ ] Capturar: Dashboard de workflows
- [ ] Capturar: Workflow completado (verde ✅)
- [ ] Capturar: Logs de ejecución
- [ ] Capturar: Resultados de tests
- [ ] Capturar: Build de Docker
- [ ] Capturar: Push a Docker Hub

## Pasos para capturar

1. Trigger workflow:
```bash
   echo "# Test" >> README.md
   git add README.md
   git commit -m "test: Trigger workflow for evidences"
   git push origin develop
```

2. Ir a: https://github.com/dantesiio/ecommerce-microservice-backend-app/actions

3. Click en el workflow que se ejecutó

4. Capturar pantallas según lista arriba

5. Guardar en: docs/evidencias/

## Verificación

- [ ] Mínimo 6 screenshots capturados
- [ ] Todos guardados en docs/evidencias/
- [ ] Nombres de archivo correctos (01-, 02-, etc.)
- [ ] Screenshots muestran mi usuario de GitHub
