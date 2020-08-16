# 在k8s中部署（minikube实践通过）

## 创建deployment
```
kubectl create -f deploy.yaml --record
```

## 查看deployment
```
kubectl get deploy
```

## 创建service
```
kubectl create -f svc.yaml --record
```

## 查看service
```
kubectl get svc
```

## 浏览器访问
```
minikube service tms-svc
```

## 查看访问服务URL
```
minikube service tms-svc --url
```

## 默认账号
```
super/88888888
```