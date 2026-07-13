# Ejecución local completa - Proyecto 2

Esta carpeta levanta en una sola computadora la topología exigida en la modalidad:

- Nginx como puerta de entrada en `http://localhost:8080`.
- Tres nodos Payara detrás de Nginx.
- Pgpool como puerta de entrada de base de datos en `localhost:5432`.
- Tres nodos PostgreSQL con replicación y failover mediante repmgr.
- Apache ActiveMQ Artemis como broker JMS compartido.
- Frontend React compilado y servido por Nginx.

## Requisitos en Windows

- Docker Desktop con WSL 2.
- Git.
- Al menos 12 GB de RAM disponibles para Docker; 16 GB es recomendable.
- Puertos libres: `5432-5435`, `61616`, `8161`, `8080-8083` y `4841-4843`.

Java, Maven, Node, Payara y PostgreSQL se ejecutan dentro de contenedores.

## Preparación

Desde PowerShell:

```powershell
git clone https://github.com/Jota2105/Sistema-Contable-Nexo.git
cd Sistema-Contable-Nexo
git checkout feature/integracion-backend-frontend
cd deploy/local
Copy-Item .env.example .env
notepad .env
```

Reemplace `GOOGLE_CLIENT_ID` por el Client ID OAuth Web de Google. Debe autorizar este origen:

```text
http://localhost:8080
```

Para pruebas iniciales puede usar `ENABLE_MOCK_LOGIN=true`. Para la grabación final debe ser `false`, porque la modalidad exige OAuth.

## Construir y arrancar

```powershell
docker compose --env-file .env up -d --build
```

Comprobar el estado:

```powershell
docker compose ps
```

Ver logs de los servidores de aplicaciones:

```powershell
docker compose logs -f payara1 payara2 payara3
```

Accesos principales:

- Sistema: `http://localhost:8080`
- Artemis Console: `http://localhost:8161`
- Payara 1 directo: `http://localhost:8081`
- Payara 2 directo: `http://localhost:8082`
- Payara 3 directo: `http://localhost:8083`
- PostgreSQL por Pgpool: `localhost:5432`, base `nexo_db`

## Balanceo de aplicaciones

El frontend consulta `GET /api/system/instance`. Al pulsar varias veces **Actualizar nodo** deben aparecer los tres valores:

```text
payara-nodo-1 | Puerto 8081
payara-nodo-2 | Puerto 8082
payara-nodo-3 | Puerto 8083
```

También puede probarlo desde PowerShell:

```powershell
1..9 | ForEach-Object {
  Invoke-RestMethod http://localhost:8080/api/system/instance
}
```

Para demostrar disponibilidad, detenga un nodo:

```powershell
docker compose stop payara1
```

El sistema debe seguir respondiendo mediante los nodos 2 y 3. Para restaurarlo:

```powershell
docker compose start payara1
```

## Base de datos y replicación

Consultar por la puerta de entrada:

```powershell
docker compose exec db-lb psql -h localhost -U postgres -d nexo_db -c "SELECT * FROM CLIENTE;"
```

Consultar directamente cada nodo:

```powershell
docker compose exec postgresql-0 psql -U postgres -d nexo_db -c "SELECT * FROM CLIENTE;"
docker compose exec postgresql-1 psql -U postgres -d nexo_db -c "SELECT * FROM CLIENTE;"
docker compose exec postgresql-2 psql -U postgres -d nexo_db -c "SELECT * FROM CLIENTE;"
```

Identificar el primario actual:

```powershell
docker compose exec postgresql-0 psql -U postgres -d nexo_db -c "SELECT pg_is_in_recovery();"
docker compose exec postgresql-1 psql -U postgres -d nexo_db -c "SELECT pg_is_in_recovery();"
docker compose exec postgresql-2 psql -U postgres -d nexo_db -c "SELECT pg_is_in_recovery();"
```

El nodo que devuelve `false` es el primario. Detenga ese nodo, espere la promoción automática de una réplica y registre un dato desde una pantalla simple. Después restaure el nodo y repita los tres `SELECT` para evidenciar que contienen la misma información.

## Integración JMS

1. Cree un artículo.
2. Registre un comprobante de ingreso para generar stock.
3. Cree una factura con ese artículo.
4. Abra `Integración > Cola de facturas`.
5. Verifique el mensaje `FACTURA_CREADA`.
6. Importe el mensaje.
7. Compruebe la creación de `VEN-{idFactura}`.
8. Consulte nuevamente la cola; el mensaje debe haber desaparecido.
9. Revise el historial de integración.

Los tres Payara utilizan el mismo broker en `tcp://artemis:61616`.

## Preparación de la grabación

La modalidad exige comenzar con las tablas operativas vacías y conservar únicamente el administrador y los catálogos necesarios. Realice esa limpieza manualmente antes de grabar y confirme con consultas SQL.

Orden sugerido para el video:

1. Mostrar `docker compose ps` con tres Payara y tres PostgreSQL.
2. Mostrar login OAuth.
3. Mostrar instancia y puerto.
4. Abrir otra pestaña y evidenciar otro nodo.
5. Detener un Payara y repetir el acceso.
6. Ejecutar CRUD de las pantallas simples.
7. Ejecutar la pantalla cabecera-detalle.
8. Mostrar consultas SQL manuales.
9. Mostrar cola, importar transacción y volver a consultar la cola.
10. Mostrar los dos reportes.
11. Detener el primario PostgreSQL, insertar un dato y restaurarlo.
12. Mostrar el mismo dato directamente en los tres nodos.

## Detener y reiniciar

Detener sin borrar datos:

```powershell
docker compose stop
```

Arrancar nuevamente:

```powershell
docker compose start
```

Eliminar contenedores conservando volúmenes:

```powershell
docker compose down
```

Para reiniciar completamente el entorno de pruebas, utilice Docker Desktop para eliminar los volúmenes del proyecto y vuelva a ejecutar `docker compose up -d --build`.
