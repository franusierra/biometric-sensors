# Biometric Sensors Infraestructure
Descargar el repositorio(instalar git si no está ya instalado):
```
git clone https://github.com/franusierra/biometric-sensors.git
```
## Requisitos para desarrollo
1. Docker: -> Instalar en [Windows](https://docs.docker.com/docker-for-windows/install/) o [Ubuntu](https://docs.docker.com/engine/install/ubuntu/)
2. (Solo windows) [Chocolatey](https://chocolatey.org/install)

3. k3d (Instalación con los siguientes comandos)

Instalar k3d en ubuntu desde la terminal:
```
curl -s https://raw.githubusercontent.com/rancher/k3d/main/install.sh \ 
    | bash
```
Instalar k3d en windows desde el cmd:
```
choco install k3d
```
## Despliegue en el desarrollo

Para el entorno de desarrollo lo más comodo es utilizar k3d para no tener que tener que instalar nada en el ordenador y crear clusters desde docker. En caso de desplegar en producción, utilizar los scripts de ansible.

Para crear el cluster de desarrollo ejecutar el siguiente comando:

```
k3d cluster create biometrics-cluster \
    -i  docker.io/rancher/k3s:v1.19.8-k3s1 \
    -p "80:80@loadbalancer" \
    -p "8080:8080@loadbalancer" \
    -p "1883:1883@loadbalancer"

```

Para comprobar que se ha creado el cluster utilizar el siguiente comando:
```
kubectl get nodes
```
Una vez preparado el cluster pueden desplegar todos los microservicios ejecutando el script `deploy.sh` desde la carpeta raíz:
```
./deploy.sh
```
O desplegando cada uno de manera independiente, permitiendo seleccionar las funcionalidades deseadas.

Namespaces tfg-edge y monitoring (Necesario)
```
kubectl apply -f k8s/namespaces
```

Secretos de autenticación (Necesario)
```
kubectl apply -f k8s/secrets
```

InfluxDB (Base de datos de sensores y alarmas):
```
kubectl apply -f k8s/deployments/influxdb
```
Grafana (Dashboard de sensores y alarmas):
```
kubectl apply -f k8s/deployments/grafana-sensors
```
Mosquitto (Broker mqtt):
```
kubectl apply -f k8s/deployments/mqtt
```
Openfaas (Motor serverless):
```
kubectl apply -f k8s/deployments/openfaas
```
Kube Prometheus Stack (Monitorización de recursos):
```
kubectl apply -f k8s/deployments/kube-prometheus-stack
```
# Acceso durante el desarrollo
Una vez terminado el despliegue deberian de estar habilitadas las siguientes rutas. Todas las cuentas por defecto son de usuario `admin` y de contraseña `politecnica`
| Servicio | Endpoint|
| :------ | :----- |
| Grafana Sensores | [localhost:80](localhost:80) |
| Gateway Openfaas | [localhost:8080](localhost:8080) |
| Grafana Monitorización | [localhost:80/monitoring](localhost:80/monitoring) |
| Mqtt (Solo desde app o cliente mqtt) | localhost:1883 |