# 在Kubernetes上安装BlackDuck的步骤及验证方法

## 一、前提条件准备

### 要求：
- Kubernetes 1.16+
- 配置了`storageClass`支持持久卷
- Helm 3

### 验证方法：
- 检查Kubernetes版本：`kubectl version`
- 检查存储类：`kubectl get storageclass`
- 检查Helm版本：`helm version`

## 二、添加BlackDuck Helm仓库

### 步骤：
```bash
helm repo add blackduck https://repo.blackduck.com/artifactory/sig-cloudnative
```

### 验证方法：
```bash
helm repo list | grep blackduck
```

## 三、下载Chart到本地

### 步骤：
```bash
helm pull blackduck/blackduck -d <目标文件夹> --untar
```

### 验证方法：
```bash
ls <目标文件夹>/blackduck
```

## 四、创建命名空间

### 步骤：
```bash
BD_NAME="bd"
kubectl create ns ${BD_NAME}
```

### 验证方法：
```bash
kubectl get ns ${BD_NAME}
```

## 五、创建自定义TLS密钥（可选）

### 步骤：
```bash
BD_NAME="bd"
kubectl create secret generic ${BD_NAME}-blackduck-webserver-certificate -n ${BD_NAME} \
  --from-file=WEBSERVER_CUSTOM_CERT_FILE=tls.crt \
  --from-file=WEBSERVER_CUSTOM_KEY_FILE=tls.key
```

### 验证方法：
```bash
kubectl get secret ${BD_NAME}-blackduck-webserver-certificate -n ${BD_NAME}
```

## 六、配置BlackDuck实例

### 1. 选择适当的部署大小

- 根据需求选择合适的scans-per-hour配置文件
- 2024.10.1版本应使用GEN05规格文件
- 注意：10sph.yaml仅用于本地测试，不适合生产环境

### 2. 配置持久存储

在values.yaml中设置：
```yaml
storageClass: "<存储类名称>"
```

### 3. 数据库配置

#### 使用外部数据库（默认）：
在values.yaml中配置：
```yaml
postgres.host: "<数据库主机>"
postgres.adminUsername: "<管理员用户名>"
postgres.adminPassword: "<管理员密码>"
postgres.userUsername: "<普通用户名>"
postgres.userPassword: "<普通用户密码>"
```

#### 使用容器化PostgreSQL：
在values.yaml中设置：
```yaml
postgres.isExternal: false
```

## 七、配置UI暴露方式

### 1. NodePort方式（默认）

在values.yaml中设置：
```yaml
exposeui: true
exposedServiceType: NodePort
exposedNodePort: "<指定端口>"
```

### 2. LoadBalancer方式

在values.yaml中设置：
```yaml
exposeui: true
exposedServiceType: LoadBalancer
```

### 3. Ingress方式

在values.yaml中设置：
```yaml
exposeui: false
```

然后创建Ingress资源：
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ${BD_NAME}-blackduck-webserver-exposed
  namespace: ${BD_NAME}
spec:
  rules:
  - host: blackduck.example.org
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ${BD_NAME}-blackduck-webserver
            port:
              number: 443
  ingressClassName: nginx
```

### 4. OpenShift路由方式

在values.yaml中设置：
```yaml
exposeui: true
exposedServiceType: OpenShift
```

## 八、安装BlackDuck Chart

### 步骤：
```bash
BD_NAME="bd" 
BD_SIZE="sizes-gen05/120sph" 
BD_INSTALL_DIR="<目标文件夹>/blackduck/"

helm install ${BD_NAME} ${BD_INSTALL_DIR} \
  --namespace ${BD_NAME} \
  -f ${BD_INSTALL_DIR}/values.yaml \
  -f ${BD_INSTALL_DIR}/${BD_SIZE}.yaml
```

### 验证方法：
```bash
helm list -n ${BD_NAME}
kubectl get pods -n ${BD_NAME}
```

## 九、访问BlackDuck UI

### 根据不同的暴露方式：

#### NodePort方式：
```
https://<节点IP>:<NodePort>
```

#### LoadBalancer方式：
获取外部IP：
```bash
kubectl get services ${BD_NAME}-blackduck-webserver-exposed -n ${BD_NAME}
```
访问：
```
https://<外部IP>
```

#### Ingress方式：
```
https://<配置的域名>
```

#### OpenShift方式：
获取路由：
```bash
oc get routes ${BD_NAME}-blackduck -n ${BD_NAME}
```
访问：
```
https://<路由主机名>
```

## 十、卸载BlackDuck

### 使用Helm卸载：
```bash
helm uninstall ${BD_NAME} --namespace ${BD_NAME}
```

### 使用kubectl卸载（如果使用dry-run生成的manifest）：
```bash
kubectl delete -f ${BD_NAME}.yaml
```

## 注意事项

1. 安装时不要使用`--wait`标志，因为这会导致安装过程无法完成
2. 定期执行数据库备份并验证备份完整性
3. 根据实际使用情况选择合适的规格配置
4. 生产环境不建议使用10sph.yaml配置

## 附录：Kubernetes常用命令介绍及使用示例

### 一、集群信息查看

#### 1. 查看集群信息
```bash
kubectl cluster-info
```
示例输出：
```
Kubernetes master is running at https://kubernetes.docker.internal:6443
KubeDNS is running at https://kubernetes.docker.internal:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

#### 2. 查看节点信息
```bash
kubectl get nodes
```
示例输出：
```
NAME             STATUS   ROLES    AGE   VERSION
docker-desktop   Ready    master   3d    v1.19.3
```

#### 3. 查看版本信息
```bash
kubectl version
```

#### 4. 查看API资源
```bash
kubectl api-resources
```

### 二、命名空间操作

#### 1. 查看所有命名空间
```bash
kubectl get namespaces
```

#### 2. 创建命名空间
```bash
kubectl create namespace my-namespace
```

#### 3. 删除命名空间
```bash
kubectl delete namespace my-namespace
```

### 三、Pod操作

#### 1. 查看所有Pod
```bash
kubectl get pods
kubectl get pods -n <命名空间>
kubectl get pods --all-namespaces
```

#### 2. 查看Pod详细信息
```bash
kubectl describe pod <pod名称> -n <命名空间>
```

#### 3. 创建Pod
```bash
kubectl run nginx --image=nginx
```

#### 4. 删除Pod
```bash
kubectl delete pod <pod名称> -n <命名空间>
```

#### 5. 进入Pod容器
```bash
kubectl exec -it <pod名称> -n <命名空间> -- /bin/bash
```

#### 6. 查看Pod日志
```bash
kubectl logs <pod名称> -n <命名空间>
kubectl logs -f <pod名称> -n <命名空间> # 实时查看
```

### 四、部署管理

#### 1. 查看部署
```bash
kubectl get deployments -n <命名空间>
```

#### 2. 创建部署
```bash
kubectl create deployment nginx --image=nginx
```

#### 3. 扩缩容部署
```bash
kubectl scale deployment <部署名称> --replicas=3 -n <命名空间>
```

#### 4. 更新镜像
```bash
kubectl set image deployment/<部署名称> <容器名>=<新镜像> -n <命名空间>
```

#### 5. 查看部署状态
```bash
kubectl rollout status deployment/<部署名称> -n <命名空间>
```

#### 6. 回滚部署
```bash
kubectl rollout undo deployment/<部署名称> -n <命名空间>
```

### 五、服务管理

#### 1. 查看服务
```bash
kubectl get services -n <命名空间>
```

#### 2. 创建服务
```bash
kubectl expose deployment <部署名称> --port=80 --type=NodePort -n <命名空间>
```

#### 3. 删除服务
```bash
kubectl delete service <服务名称> -n <命名空间>
```

#### 4. 查看服务详情
```bash
kubectl describe service <服务名称> -n <命名空间>
```

### 六、配置管理

#### 1. 查看ConfigMap
```bash
kubectl get configmaps -n <命名空间>
```

#### 2. 创建ConfigMap
```bash
kubectl create configmap <名称> --from-file=<文件路径> -n <命名空间>
kubectl create configmap <名称> --from-literal=key1=value1 --from-literal=key2=value2 -n <命名空间>
```

#### 3. 查看Secret
```bash
kubectl get secrets -n <命名空间>
```

#### 4. 创建Secret
```bash
kubectl create secret generic <名称> --from-literal=username=admin --from-literal=password=secret -n <命名空间>
```

### 七、存储操作

#### 1. 查看PersistentVolume
```bash
kubectl get pv
```

#### 2. 查看PersistentVolumeClaim
```bash
kubectl get pvc -n <命名空间>
```

#### 3. 查看StorageClass
```bash
kubectl get storageclass
```

#### 4. 创建StorageClass
```bash
kubectl apply -f storageclass.yaml
```
示例storageclass.yaml文件:
```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: standard
provisioner: kubernetes.io/no-provisioner
volumeBindingMode: WaitForFirstConsumer
```

### 八、日志和调试

#### 1. 查看Pod日志
```bash
kubectl logs <pod名称> -n <命名空间>
kubectl logs -f <pod名称> -c <容器名称> -n <命名空间> # 多容器Pod指定容器
```

#### 2. 端口转发
```bash
kubectl port-forward <pod名称> 8080:80 -n <命名空间>
```

#### 3. 查看事件
```bash
kubectl get events -n <命名空间>
```

#### 4. 查看资源使用情况
```bash
kubectl top nodes
kubectl top pods -n <命名空间>
```

### 九、应用配置

#### 1. 使用配置文件
```bash
kubectl apply -f <配置文件.yaml>
```

#### 2. 查看配置文件内容
```bash
kubectl get deployment <部署名称> -o yaml
```

#### 3. 编辑资源
```bash
kubectl edit deployment <部署名称> -n <命名空间>
```

### 十、上下文和配置

#### 1. 查看当前上下文
```bash
kubectl config current-context
```

#### 2. 查看所有上下文
```bash
kubectl config get-contexts
```

#### 3. 切换上下文
```bash
kubectl config use-context <上下文名称>
```

### 十一、标签和选择器

#### 1. 为资源添加标签
```bash
kubectl label pods <pod名称> environment=production -n <命名空间>
```

#### 2. 使用选择器查询资源
```bash
kubectl get pods -l environment=production -n <命名空间>
```

### 十二、网络策略

#### 1. 查看网络策略
```bash
kubectl get networkpolicies -n <命名空间>
```

#### 2. 应用网络策略
```bash
kubectl apply -f networkpolicy.yaml
```

### 使用提示

1. 使用`-o wide`参数可以获得更详细的输出：
   ```bash
   kubectl get pods -o wide
   ```

2. 使用`--watch`或`-w`参数可以监视资源变化：
   ```bash
   kubectl get pods -w
   ```

3. 使用`--dry-run=client -o yaml`参数可以生成配置文件而不实际创建资源：
   ```bash
   kubectl create deployment nginx --image=nginx --dry-run=client -o yaml > nginx-deployment.yaml
   ```

4. 使用`kubectl explain`查看资源定义：
   ```bash
   kubectl explain deployment.spec
   ```

5. 使用`--field-selector`筛选资源：
   ```bash
   kubectl get pods --field-selector status.phase=Running
   ``` 