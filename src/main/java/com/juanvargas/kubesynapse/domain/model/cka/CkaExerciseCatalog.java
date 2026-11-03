package com.juanvargas.kubesynapse.domain.model.cka;

import java.util.List;

/**
 * ======================================================================================
 * CATÁLOGO MAESTRO DE EJERCICIOS REALES CKA (SEPTEMBER 2026 - CURRICULUM CNCF)
 * ======================================================================================
 * Escenarios prácticos basados en los estándares de administración de Kubernetes (CNCF / Linux Foundation).
 * Cubre los 5 dominios de administración Cloud Native con enunciados técnicos bilingües.
 * ======================================================================================
 */
public class CkaExerciseCatalog {

    public static List<CkaExercise> getAllExercises() {
        return List.of(
                // ----------------------------------------------------------------------
                // DOMINIO 1: TROUBLESHOOTING (30% del programa CKA)
                // ----------------------------------------------------------------------
                CkaExercise.builder()
                        .id("CKA-TROUBLE-01")
                        .domain(CkaDomain.TROUBLESHOOTING)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(8)
                        .timeLimitMinutes(7)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-troubleshoot")
                        .targetResource("api-gateway")
                        .titleEn("Fix CrashLoopBackOff Pod Due to Broken Startup Command")
                        .titleEs("Corregir Pod en CrashLoopBackOff por Comando de Inicio Erróneo")
                        .scenarioEn("""
                                Context: An internal microservice pod named 'api-gateway' in namespace 'cka-troubleshoot' fails to start and enters CrashLoopBackOff.
                                Task:
                                1. Inspect the pod events and logs to determine the cause of the failure.
                                2. Correct the pod configuration so that it starts successfully.
                                3. The container must execute a command that stays alive (e.g., 'sleep 3600' or an nginx web process).
                                4. Verify that the pod reaches the 'Running' state with 1/1 containers Ready and 0 exit errors.
                                """)
                        .scenarioEs("""
                                Contexto: Un pod de microservicio interno llamado 'api-gateway' en el namespace 'cka-troubleshoot' falla al iniciar y entra en estado CrashLoopBackOff.
                                Tarea:
                                1. Inspecciona los eventos y logs del pod para determinar la causa raíz del fallo.
                                2. Corrige la configuración del pod para que inicie exitosamente.
                                3. El contenedor debe ejecutar un comando que permanezca activo (por ejemplo: 'sleep 3600' o proceso nginx).
                                4. Verifica que el pod alcance el estado 'Running' con 1/1 contenedores Ready y sin errores de salida.
                                """)
                        .officialSolutionEn("""
                                # 1. Check pod status and logs:
                                kubectl get pods -n cka-troubleshoot
                                kubectl logs api-gateway -n cka-troubleshoot
                                kubectl describe pod api-gateway -n cka-troubleshoot
                                
                                # 2. Extract YAML, fix command to ["sh", "-c", "sleep 3600"] or delete and recreate:
                                kubectl get pod api-gateway -n cka-troubleshoot -o yaml > fix-pod.yaml
                                # Edit fix-pod.yaml -> replace failing command with 'sleep 3600'
                                kubectl replace --force -f fix-pod.yaml
                                
                                # 3. Confirm Running state:
                                kubectl get pod api-gateway -n cka-troubleshoot
                                """)
                        .officialSolutionEs("""
                                # 1. Diagnosticar estado y causa de terminación:
                                kubectl get pods -n cka-troubleshoot
                                kubectl logs api-gateway -n cka-troubleshoot
                                kubectl describe pod api-gateway -n cka-troubleshoot
                                
                                # 2. Exportar YAML, corregir el comando erróneo por 'sleep 3600' y recrear:
                                kubectl get pod api-gateway -n cka-troubleshoot -o yaml > pod.yaml
                                # Modificar command en el yaml y aplicar:
                                kubectl replace --force -f pod.yaml
                                
                                # 3. Verificar estado Running:
                                kubectl get pod api-gateway -n cka-troubleshoot
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: api-gateway
                                  namespace: cka-troubleshoot
                                  labels:
                                    app: api-gateway
                                spec:
                                  containers:
                                  - name: gateway
                                    image: busybox:1.36
                                    command: ["sh", "-c", "echo 'Gateway started successfully' && sleep 3600"]
                                """)
                        .fastExamTipEn("In CKA, use 'kubectl get pod <name> -o yaml > pod.yaml' followed by 'kubectl replace --force -f pod.yaml' to edit immutable pod spec fields in under 60 seconds!")
                        .fastExamTipEs("En entornos de producción y troubleshooting, usa 'kubectl get pod <name> -o yaml > pod.yaml' y luego 'kubectl replace --force -f pod.yaml' para modificar campos inmutables del pod en menos de 60 segundos!")
                        .verificationCriteria(List.of(
                                "Namespace 'cka-troubleshoot' exists",
                                "Pod 'api-gateway' exists and has status Phase = 'Running'",
                                "Pod 'api-gateway' container is Ready (1/1)",
                                "Pod restart count is stable without exit code 1"
                        ))
                        .build(),

                CkaExercise.builder()
                        .id("CKA-TROUBLE-02")
                        .domain(CkaDomain.TROUBLESHOOTING)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(7)
                        .timeLimitMinutes(6)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-troubleshoot")
                        .targetResource("worker-cache")
                        .titleEn("Resolve OOMKilled Memory Limit Exception on Worker Pod")
                        .titleEs("Resolver Excepción OOMKilled por Límite de Memoria en Pod Worker")
                        .scenarioEn("""
                                Context: A batch worker pod named 'worker-cache' in namespace 'cka-troubleshoot' crashes immediately after creation with ExitCode 137.
                                Task:
                                1. Inspect the last termination reason of the pod.
                                2. Increase the container memory limit to at least 256Mi and memory request to 128Mi.
                                3. Recreate the pod and verify that it remains in 'Running' state without being OOMKilled.
                                """)
                        .scenarioEs("""
                                Contexto: Un pod worker batch llamado 'worker-cache' en el namespace 'cka-troubleshoot' crashea inmediatamente tras su creación con ExitCode 137.
                                Tarea:
                                1. Inspecciona la última razón de terminación (Last State: Terminated / Reason: OOMKilled).
                                2. Incrementa el límite de memoria del contenedor a por lo menos 256Mi y la solicitud de memoria a 128Mi.
                                3. Recrea el pod y verifica que permanezca en estado 'Running' sin ser liquidado por falta de memoria.
                                """)
                        .officialSolutionEn("""
                                # 1. Verify OOMKilled exit code 137:
                                kubectl describe pod worker-cache -n cka-troubleshoot | grep -i -E "exit code|oomkilled|limits"
                                
                                # 2. Extract and fix memory resources:
                                kubectl get pod worker-cache -n cka-troubleshoot -o yaml > worker.yaml
                                # Change memory limits to 256Mi and requests to 128Mi
                                kubectl replace --force -f worker.yaml
                                
                                # 3. Confirm Running:
                                kubectl get pod worker-cache -n cka-troubleshoot
                                """)
                        .officialSolutionEs("""
                                # 1. Comprobar que falló con Exit Code 137 (OOMKilled):
                                kubectl describe pod worker-cache -n cka-troubleshoot | grep -i -E "exit code|oomkilled"
                                
                                # 2. Ajustar los límites de memoria a 256Mi:
                                kubectl get pod worker-cache -n cka-troubleshoot -o yaml > worker.yaml
                                # Modificar resources.limits.memory: 256Mi
                                kubectl replace --force -f worker.yaml
                                
                                # 3. Verificar pod estable:
                                kubectl get pod worker-cache -n cka-troubleshoot
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: worker-cache
                                  namespace: cka-troubleshoot
                                spec:
                                  containers:
                                  - name: cache-worker
                                    image: busybox:1.36
                                    command: ["sh", "-c", "sleep 3600"]
                                    resources:
                                      requests:
                                        memory: "128Mi"
                                        cpu: "100m"
                                      limits:
                                        memory: "256Mi"
                                        cpu: "250m"
                                """)
                        .fastExamTipEn("Exit Code 137 always means SIGKILL (128 + 9), predominantly triggered by the Linux OOM Killer when exceeding container cgroup memory limits.")
                        .fastExamTipEs("El Exit Code 137 siempre significa SIGKILL (128 + 9), provocado en el 99% de los casos por el Linux OOM Killer al superar el límite de memoria del cgroup.")
                        .verificationCriteria(List.of(
                                "Pod 'worker-cache' exists in namespace 'cka-troubleshoot'",
                                "Pod memory limit is set to at least 256Mi",
                                "Pod is in 'Running' state without restarts or termination code 137"
                        ))
                        .build(),

                // ----------------------------------------------------------------------
                // DOMINIO 2: CLUSTER ARCHITECTURE, INSTALLATION & CONFIG (25% del programa CKA)
                // ----------------------------------------------------------------------
                CkaExercise.builder()
                        .id("CKA-ARCH-01")
                        .domain(CkaDomain.CLUSTER_ARCHITECTURE)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(7)
                        .timeLimitMinutes(5)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-security")
                        .targetResource("pod-reader")
                        .titleEn("Configure RBAC Role & RoleBinding for Developer ServiceAccount")
                        .titleEs("Configurar Role y RoleBinding de RBAC para ServiceAccount de Desarrollador")
                        .scenarioEn("""
                                Context: The development team needs limited access to inspect pods in the 'cka-security' namespace.
                                Task:
                                1. Create a ServiceAccount named 'developer-sa' in namespace 'cka-security' (if not already present).
                                2. Create a Role named 'pod-reader' in namespace 'cka-security' granting permissions to 'get', 'list', and 'watch' on 'pods'.
                                3. Create a RoleBinding named 'developer-pod-binding' in namespace 'cka-security' binding the Role 'pod-reader' to the ServiceAccount 'developer-sa'.
                                4. Verify access using: 'kubectl auth can-i list pods -n cka-security --as=system:serviceaccount:cka-security:developer-sa'.
                                """)
                        .scenarioEs("""
                                Contexto: El equipo de desarrollo necesita acceso restringido para inspeccionar pods en el namespace 'cka-security'.
                                Tarea:
                                1. Crea un ServiceAccount llamado 'developer-sa' en el namespace 'cka-security' (si no existe).
                                2. Crea un Role llamado 'pod-reader' en el namespace 'cka-security' que conceda permisos de 'get', 'list' y 'watch' sobre el recurso 'pods'.
                                3. Crea un RoleBinding llamado 'developer-pod-binding' en 'cka-security' enlazando el Role 'pod-reader' con el ServiceAccount 'developer-sa'.
                                4. Verifica el acceso ejecutando: 'kubectl auth can-i list pods -n cka-security --as=system:serviceaccount:cka-security:developer-sa'.
                                """)
                        .officialSolutionEn("""
                                # 1. Create ServiceAccount:
                                kubectl create sa developer-sa -n cka-security
                                
                                # 2. Create Role imperatively:
                                kubectl create role pod-reader -n cka-security --verb=get,list,watch --resource=pods
                                
                                # 3. Create RoleBinding imperatively:
                                kubectl create rolebinding developer-pod-binding -n cka-security --role=pod-reader --serviceaccount=cka-security:developer-sa
                                
                                # 4. Test authorization:
                                kubectl auth can-i list pods -n cka-security --as=system:serviceaccount:cka-security:developer-sa
                                """)
                        .officialSolutionEs("""
                                # 1. Crear ServiceAccount de forma imperativa:
                                kubectl create sa developer-sa -n cka-security
                                
                                # 2. Crear Role con los verbos requeridos:
                                kubectl create role pod-reader -n cka-security --verb=get,list,watch --resource=pods
                                
                                # 3. Enlazar con RoleBinding:
                                kubectl create rolebinding developer-pod-binding -n cka-security --role=pod-reader --serviceaccount=cka-security:developer-sa
                                
                                # 4. Probar autorización con auth can-i:
                                kubectl auth can-i list pods -n cka-security --as=system:serviceaccount:cka-security:developer-sa
                                """)
                        .yamlSolution("""
                                apiVersion: rbac.authorization.k8s.io/v1
                                kind: Role
                                metadata:
                                  name: pod-reader
                                  namespace: cka-security
                                rules:
                                - apiGroups: [""]
                                  resources: ["pods"]
                                  verbs: ["get", "list", "watch"]
                                ---
                                apiVersion: rbac.authorization.k8s.io/v1
                                kind: RoleBinding
                                metadata:
                                  name: developer-pod-binding
                                  namespace: cka-security
                                subjects:
                                - kind: ServiceAccount
                                  name: developer-sa
                                  namespace: cka-security
                                roleRef:
                                  kind: Role
                                  name: pod-reader
                                  apiGroup: rbac.authorization.k8s.io
                                """)
                        .fastExamTipEn("Always use imperative commands for fast RBAC troubleshooting: 'kubectl create role ... --verb=... --resource=...' saves 3+ minutes compared to writing YAML by hand.")
                        .fastExamTipEs("¡Utiliza comandos imperativos para agilizar operaciones de RBAC! 'kubectl create role ...' y 'kubectl create rolebinding ...' te ahorran más de 3 minutos.")
                        .verificationCriteria(List.of(
                                "ServiceAccount 'developer-sa' exists in 'cka-security'",
                                "Role 'pod-reader' exists in 'cka-security' with verbs [get, list, watch] on pods",
                                "RoleBinding 'developer-pod-binding' connects ServiceAccount to Role 'pod-reader'",
                                "kubectl auth can-i evaluates to 'yes' for developer-sa"
                        ))
                        .build(),

                // ----------------------------------------------------------------------
                // DOMINIO 3: SERVICES & NETWORKING (20% del programa CKA)
                // ----------------------------------------------------------------------
                CkaExercise.builder()
                        .id("CKA-NET-01")
                        .domain(CkaDomain.SERVICES_AND_NETWORKING)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(7)
                        .timeLimitMinutes(5)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-networking")
                        .targetResource("web-service")
                        .titleEn("Repair Misconfigured Service Selector & Endpoints")
                        .titleEs("Reparar Selector de Servicio Desalineado y Endpoints Vacíos")
                        .scenarioEn("""
                                Context: Frontend clients report that 'web-service' in namespace 'cka-networking' returns connection timeouts.
                                Task:
                                1. Inspect the Service 'web-service' and its Endpoints.
                                2. Identify why no endpoints are attached to the service.
                                3. Fix the label selector on the Service so that it matches the running deployment pods.
                                4. Verify that 'kubectl get endpoints web-service -n cka-networking' displays at least 1 healthy pod endpoint IP.
                                """)
                        .scenarioEs("""
                                Contexto: Los clientes de frontend reportan que 'web-service' en el namespace 'cka-networking' genera timeouts de conexión.
                                Tarea:
                                1. Inspecciona el Service 'web-service' y sus Endpoints.
                                2. Identifica por qué no hay ningún endpoint asociado al servicio.
                                3. Corrige el selector de etiquetas (selector) del Service para que coincida exactamente con las etiquetas de los pods del deployment.
                                4. Verifica que 'kubectl get endpoints web-service -n cka-networking' muestre al menos 1 IP de pod saludable.
                                """)
                        .officialSolutionEn("""
                                # 1. Check endpoints and service selector:
                                kubectl get endpoints web-service -n cka-networking
                                kubectl describe svc web-service -n cka-networking | grep Selector
                                
                                # 2. Check actual pod labels:
                                kubectl get pods -n cka-networking --show-labels
                                
                                # 3. Update service selector to match pod label (e.g. app=web-frontend):
                                kubectl set selector svc web-service app=web-frontend -n cka-networking
                                
                                # 4. Verify endpoints:
                                kubectl get endpoints web-service -n cka-networking
                                """)
                        .officialSolutionEs("""
                                # 1. Verificar endpoints vacíos:
                                kubectl get endpoints web-service -n cka-networking
                                kubectl describe svc web-service -n cka-networking | grep Selector
                                
                                # 2. Inspeccionar etiquetas reales de los pods:
                                kubectl get pods -n cka-networking --show-labels
                                
                                # 3. Corregir el selector del servicio (ejemplo app=web-frontend):
                                kubectl set selector svc web-service app=web-frontend -n cka-networking
                                
                                # 4. Verificar que aparezcan los endpoints IP:
                                kubectl get endpoints web-service -n cka-networking
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Service
                                metadata:
                                  name: web-service
                                  namespace: cka-networking
                                spec:
                                  type: ClusterIP
                                  selector:
                                    app: web-frontend
                                  ports:
                                  - port: 80
                                    targetPort: 80
                                """)
                        .fastExamTipEn("Use 'kubectl set selector svc <svc-name> <key>=<val>' to update a Service selector on-the-fly without opening vim!")
                        .fastExamTipEs("¡Utiliza 'kubectl set selector svc <svc-name> <key>=<val>' para modificar el selector de un Service al vuelo sin necesidad de abrir un editor!")
                        .verificationCriteria(List.of(
                                "Service 'web-service' exists in 'cka-networking'",
                                "Service selector matches pod label 'app=web-frontend'",
                                "Endpoints object 'web-service' has at least 1 active target IP address"
                        ))
                        .build(),

                CkaExercise.builder()
                        .id("CKA-NET-02")
                        .domain(CkaDomain.SERVICES_AND_NETWORKING)
                        .difficulty(CkaDifficulty.HARD)
                        .weightPoints(8)
                        .timeLimitMinutes(8)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-security")
                        .targetResource("db-netpol")
                        .titleEn("Enforce Ingress NetworkPolicy for Database Isolation")
                        .titleEs("Implementar NetworkPolicy de Ingress para Aislamiento de Base de Datos")
                        .scenarioEn("""
                                Context: In namespace 'cka-security', a PostgreSQL pod with label 'app=database' is exposed to all network traffic.
                                Task:
                                1. Create a NetworkPolicy named 'db-netpol' in namespace 'cka-security'.
                                2. Apply the policy to pods with label 'app=database'.
                                3. Allow incoming (Ingress) TCP traffic on port 5432 ONLY from pods with label 'app=frontend'.
                                4. Ensure all other ingress traffic to the database is denied.
                                """)
                        .scenarioEs("""
                                Contexto: En el namespace 'cka-security', un pod PostgreSQL con etiqueta 'app=database' está expuesto a todo el tráfico de la red.
                                Tarea:
                                1. Crea una NetworkPolicy llamada 'db-netpol' en el namespace 'cka-security'.
                                2. Aplica la directiva a los pods con la etiqueta 'app=database'.
                                3. Permite tráfico entrante (Ingress) TCP en el puerto 5432 ÚNICAMENTE desde pods con la etiqueta 'app=frontend'.
                                4. Asegura que todo otro tráfico entrante hacia la base de datos quede bloqueado (Default Deny).
                                """)
                        .officialSolutionEn("""
                                # Apply the NetworkPolicy YAML:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: networking.k8s.io/v1
                                kind: NetworkPolicy
                                metadata:
                                  name: db-netpol
                                  namespace: cka-security
                                spec:
                                  podSelector:
                                    matchLabels:
                                      app: database
                                  policyTypes:
                                  - Ingress
                                  ingress:
                                  - from:
                                    - podSelector:
                                        matchLabels:
                                          app: frontend
                                    ports:
                                    - protocol: TCP
                                      port: 5432
                                EOF
                                """)
                        .officialSolutionEs("""
                                # Aplicar el manifiesto NetworkPolicy:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: networking.k8s.io/v1
                                kind: NetworkPolicy
                                metadata:
                                  name: db-netpol
                                  namespace: cka-security
                                spec:
                                  podSelector:
                                    matchLabels:
                                      app: database
                                  policyTypes:
                                  - Ingress
                                  ingress:
                                  - from:
                                    - podSelector:
                                        matchLabels:
                                          app: frontend
                                    ports:
                                    - protocol: TCP
                                      port: 5432
                                EOF
                                """)
                        .yamlSolution("""
                                apiVersion: networking.k8s.io/v1
                                kind: NetworkPolicy
                                metadata:
                                  name: db-netpol
                                  namespace: cka-security
                                spec:
                                  podSelector:
                                    matchLabels:
                                      app: database
                                  policyTypes:
                                  - Ingress
                                  ingress:
                                  - from:
                                    - podSelector:
                                        matchLabels:
                                          app: frontend
                                    ports:
                                    - protocol: TCP
                                      port: 5432
                                """)
                        .fastExamTipEn("In NetworkPolicy, when 'policyTypes: [Ingress]' is specified, an empty 'from:' allows all traffic, while omitting 'from:' completely blocks all ingress traffic.")
                        .fastExamTipEs("En NetworkPolicy, si especificas 'policyTypes: [Ingress]', un bloque 'from:' vacío permite todo el tráfico, mientras que omitir el bloque 'from:' bloquea absolutamente todo el tráfico entrante.")
                        .verificationCriteria(List.of(
                                "NetworkPolicy 'db-netpol' exists in 'cka-security'",
                                "podSelector selects 'app=database'",
                                "Ingress rule restricts traffic to TCP port 5432 from 'app=frontend'"
                        ))
                        .build(),

                // ----------------------------------------------------------------------
                // DOMINIO 4: WORKLOADS & SCHEDULING (15% del programa CKA)
                // ----------------------------------------------------------------------
                CkaExercise.builder()
                        .id("CKA-SCHED-01")
                        .domain(CkaDomain.WORKLOADS_AND_SCHEDULING)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(6)
                        .timeLimitMinutes(6)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-scheduling")
                        .targetResource("tolerator-pod")
                        .titleEn("Schedule Pod with Node Taints and Tolerations")
                        .titleEs("Programar Pod con Tolerancias ante Taints de Nodo")
                        .scenarioEn("""
                                Context: A node is tainted with 'tier=backend:NoSchedule'. Normal pods cannot be scheduled onto it.
                                Task:
                                1. In namespace 'cka-scheduling', create a pod named 'tolerator-pod' running image 'nginx:alpine'.
                                2. Configure a toleration on the pod so it can be scheduled on the tainted node.
                                3. Key: 'tier', Value: 'backend', Effect: 'NoSchedule', Operator: 'Equal'.
                                4. Verify that 'tolerator-pod' successfully reaches state 'Running'.
                                """)
                        .scenarioEs("""
                                Contexto: Un nodo tiene el taint 'tier=backend:NoSchedule'. Los pods normales no pueden ser asignados a este nodo.
                                Tarea:
                                1. En el namespace 'cka-scheduling', crea un pod llamado 'tolerator-pod' con la imagen 'nginx:alpine'.
                                2. Configura una toleration en el pod para que el scheduler pueda asignarlo al nodo con taint.
                                3. Key: 'tier', Value: 'backend', Effect: 'NoSchedule', Operator: 'Equal'.
                                4. Verifica que 'tolerator-pod' alcance exitosamente el estado 'Running'.
                                """)
                        .officialSolutionEn("""
                                # 1. Generate pod manifest imperatively:
                                kubectl run tolerator-pod --image=nginx:alpine --restart=Never --dry-run=client -o yaml > pod.yaml
                                
                                # 2. Add tolerations section under spec:
                                # tolerations:
                                # - key: "tier"
                                #   operator: "Equal"
                                #   value: "backend"
                                #   effect: "NoSchedule"
                                
                                # 3. Apply:
                                kubectl apply -f pod.yaml -n cka-scheduling
                                kubectl get pod tolerator-pod -n cka-scheduling
                                """)
                        .officialSolutionEs("""
                                # 1. Generar plantilla de pod imperativa:
                                kubectl run tolerator-pod --image=nginx:alpine --restart=Never --dry-run=client -o yaml > pod.yaml
                                
                                # 2. Agregar sección tolerations bajo spec:
                                # tolerations:
                                # - key: "tier"
                                #   operator: "Equal"
                                #   value: "backend"
                                #   effect: "NoSchedule"
                                
                                # 3. Aplicar:
                                kubectl apply -f pod.yaml -n cka-scheduling
                                kubectl get pod tolerator-pod -n cka-scheduling
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: tolerator-pod
                                  namespace: cka-scheduling
                                spec:
                                  containers:
                                  - name: nginx
                                    image: nginx:alpine
                                  tolerations:
                                  - key: "tier"
                                    operator: "Equal"
                                    value: "backend"
                                    effect: "NoSchedule"
                                """)
                        .fastExamTipEn("Always use 'kubectl run ... --dry-run=client -o yaml' to generate base manifests quickly instead of writing from scratch.")
                        .fastExamTipEs("Genera siempre la base con 'kubectl run ... --dry-run=client -o yaml' para evitar errores de sintaxis en el YAML.")
                        .verificationCriteria(List.of(
                                "Pod 'tolerator-pod' exists in namespace 'cka-scheduling'",
                                "Pod contains toleration for key 'tier' and effect 'NoSchedule'",
                                "Pod is in 'Running' phase"
                        ))
                        .build(),

                // ----------------------------------------------------------------------
                // DOMINIO 5: STORAGE (10% del programa CKA)
                // ----------------------------------------------------------------------
                CkaExercise.builder()
                        .id("CKA-STORE-01")
                        .domain(CkaDomain.STORAGE)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(6)
                        .timeLimitMinutes(6)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-storage")
                        .targetResource("pvc-analytics")
                        .titleEn("Create PersistentVolume and Bind via PersistentVolumeClaim")
                        .titleEs("Crear PersistentVolume y Enlazar mediante PersistentVolumeClaim")
                        .scenarioEn("""
                                Context: An analytics application requires persistent host storage.
                                Task:
                                1. Create a PersistentVolume named 'pv-analytics' with capacity '1Gi', accessMode 'ReadWriteOnce', hostPath '/data/pv-analytics', and storageClassName 'manual'.
                                2. In namespace 'cka-storage', create a PersistentVolumeClaim named 'pvc-analytics' requesting '500Mi', accessMode 'ReadWriteOnce', and storageClassName 'manual'.
                                3. Verify that the PVC status transitions to 'Bound'.
                                """)
                        .scenarioEs("""
                                Contexto: Una aplicación analítica requiere almacenamiento persistente en el host.
                                Tarea:
                                1. Crea un PersistentVolume llamado 'pv-analytics' con capacidad '1Gi', accessMode 'ReadWriteOnce', hostPath '/data/pv-analytics' y storageClassName 'manual'.
                                2. En el namespace 'cka-storage', crea un PersistentVolumeClaim llamado 'pvc-analytics' que solicite '500Mi', accessMode 'ReadWriteOnce' y storageClassName 'manual'.
                                3. Verifica que el estado del PVC transicione a 'Bound'.
                                """)
                        .officialSolutionEn("""
                                # Apply PV and PVC manifests:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: v1
                                kind: PersistentVolume
                                metadata:
                                  name: pv-analytics
                                spec:
                                  capacity:
                                    storage: 1Gi
                                  accessModes:
                                    - ReadWriteOnce
                                  storageClassName: manual
                                  hostPath:
                                    path: /data/pv-analytics
                                ---
                                apiVersion: v1
                                kind: PersistentVolumeClaim
                                metadata:
                                  name: pvc-analytics
                                  namespace: cka-storage
                                spec:
                                  accessModes:
                                    - ReadWriteOnce
                                  storageClassName: manual
                                  resources:
                                    requests:
                                      storage: 500Mi
                                EOF
                                
                                # Verify binding:
                                kubectl get pv pv-analytics
                                kubectl get pvc pvc-analytics -n cka-storage
                                """)
                        .officialSolutionEs("""
                                # Aplicar los manifiestos de PV y PVC:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: v1
                                kind: PersistentVolume
                                metadata:
                                  name: pv-analytics
                                spec:
                                  capacity:
                                    storage: 1Gi
                                  accessModes:
                                    - ReadWriteOnce
                                  storageClassName: manual
                                  hostPath:
                                    path: /data/pv-analytics
                                ---
                                apiVersion: v1
                                kind: PersistentVolumeClaim
                                metadata:
                                  name: pvc-analytics
                                  namespace: cka-storage
                                spec:
                                  accessModes:
                                    - ReadWriteOnce
                                  storageClassName: manual
                                  resources:
                                    requests:
                                      storage: 500Mi
                                EOF
                                
                                # Verificar que esté Bound:
                                kubectl get pv pv-analytics
                                kubectl get pvc pvc-analytics -n cka-storage
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: PersistentVolume
                                metadata:
                                  name: pv-analytics
                                spec:
                                  capacity:
                                    storage: 1Gi
                                  accessModes:
                                    - ReadWriteOnce
                                  storageClassName: manual
                                  hostPath:
                                    path: /data/pv-analytics
                                ---
                                apiVersion: v1
                                kind: PersistentVolumeClaim
                                metadata:
                                  name: pvc-analytics
                                  namespace: cka-storage
                                spec:
                                  accessModes:
                                    - ReadWriteOnce
                                  storageClassName: manual
                                  resources:
                                    requests:
                                      storage: 500Mi
                                """)
                        .fastExamTipEn("For a PVC to bind to a PV, storageClassName must match exactly, capacity must be <= PV capacity, and accessModes must be compatible.")
                        .fastExamTipEs("Para que un PVC se enlace (Bound) a un PV, el storageClassName debe coincidir exactamente, la capacidad solicitada debe ser menor o igual a la del PV y los accessModes deben ser compatibles.")
                        .verificationCriteria(List.of(
                                "PersistentVolume 'pv-analytics' exists with capacity 1Gi",
                                "PersistentVolumeClaim 'pvc-analytics' exists in 'cka-storage'",
                                "PVC status is 'Bound'"
                        ))
                        .build(),

                // ----------------------------------------------------------------------
                // EJERCICIOS ADICIONALES CKA (CURRICULUM OFICIAL SEPTIEMBRE 2026)
                // ----------------------------------------------------------------------
                CkaExercise.builder()
                        .id("CKA-TROUBLE-03")
                        .domain(CkaDomain.TROUBLESHOOTING)
                        .difficulty(CkaDifficulty.EASY)
                        .weightPoints(6)
                        .timeLimitMinutes(4)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-troubleshoot")
                        .targetResource("web-frontend-broken")
                        .titleEn("Resolve ImagePullBackOff Container Error")
                        .titleEs("Resolver Error ImagePullBackOff en Contenedor Web")
                        .scenarioEn("""
                                Context: A pod named 'web-frontend-broken' in namespace 'cka-troubleshoot' is stuck in ImagePullBackOff.
                                Task:
                                1. Inspect the pod events to identify the faulty container image tag.
                                2. Update the image to 'nginx:1.27-alpine' (or valid nginx alpine image).
                                3. Ensure the pod transitions to 'Running' with 1/1 containers Ready.
                                """)
                        .scenarioEs("""
                                Contexto: Un pod llamado 'web-frontend-broken' en el namespace 'cka-troubleshoot' está atascado en ImagePullBackOff.
                                Tarea:
                                1. Inspecciona los eventos del pod para identificar la etiqueta de imagen errónea.
                                2. Actualiza la imagen del contenedor a 'nginx:1.27-alpine' (o versión válida de nginx alpine).
                                3. Asegura que el pod transicione a 'Running' con 1/1 contenedores Ready.
                                """)
                        .officialSolutionEn("""
                                # 1. Check pod events:
                                kubectl describe pod web-frontend-broken -n cka-troubleshoot | grep -i failed
                                # 2. Update image dynamically:
                                kubectl set image pod/web-frontend-broken web-server=nginx:alpine -n cka-troubleshoot
                                # 3. Confirm Running:
                                kubectl get pod web-frontend-broken -n cka-troubleshoot
                                """)
                        .officialSolutionEs("""
                                # 1. Inspeccionar el evento de fallo de descarga:
                                kubectl describe pod web-frontend-broken -n cka-troubleshoot | grep -i failed
                                # 2. Actualizar imagen directamente:
                                kubectl set image pod/web-frontend-broken web-server=nginx:alpine -n cka-troubleshoot
                                # 3. Confirmar estado Running:
                                kubectl get pod web-frontend-broken -n cka-troubleshoot
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: web-frontend-broken
                                  namespace: cka-troubleshoot
                                spec:
                                  containers:
                                  - name: web-server
                                    image: nginx:alpine
                                """)
                        .fastExamTipEn("Use 'kubectl set image pod/<pod-name> <container-name>=<new-image>' to fix images on the fly without deleting the pod!")
                        .fastExamTipEs("¡Usa 'kubectl set image pod/<pod> <container>=<imagen>' para corregir la imagen al vuelo sin tener que borrar el pod!")
                        .verificationCriteria(List.of(
                                "Pod 'web-frontend-broken' exists in 'cka-troubleshoot'",
                                "Container image does not contain invalid tag",
                                "Pod phase is 'Running' and container is Ready"
                        ))
                        .build(),

                CkaExercise.builder()
                        .id("CKA-ARCH-02")
                        .domain(CkaDomain.CLUSTER_ARCHITECTURE)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(8)
                        .timeLimitMinutes(6)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("default")
                        .targetResource("deployment-auditor")
                        .titleEn("Configure ClusterRole & ClusterRoleBinding for Global Auditor")
                        .titleEs("Configurar ClusterRole y ClusterRoleBinding para Auditor Global")
                        .scenarioEn("""
                                Context: Security auditors need cluster-wide read access to all Deployments and DaemonSets.
                                Task:
                                1. Create a ClusterRole named 'deployment-auditor' that allows 'get', 'list', and 'watch' on 'deployments' and 'daemonsets' in apiGroup 'apps'.
                                2. In namespace 'default', create a ServiceAccount named 'auditor-sa' (if not present).
                                3. Create a ClusterRoleBinding named 'auditor-global-binding' binding 'deployment-auditor' to 'default:auditor-sa'.
                                4. Test with: 'kubectl auth can-i list deployments --as=system:serviceaccount:default:auditor-sa -A'.
                                """)
                        .scenarioEs("""
                                Contexto: Los auditores de seguridad necesitan acceso de lectura en todo el clúster sobre Deployments y DaemonSets.
                                Tarea:
                                1. Crea un ClusterRole llamado 'deployment-auditor' que permita 'get', 'list' y 'watch' sobre 'deployments' y 'daemonsets' en el apiGroup 'apps'.
                                2. En el namespace 'default', crea un ServiceAccount llamado 'auditor-sa'.
                                3. Crea un ClusterRoleBinding llamado 'auditor-global-binding' vinculando 'deployment-auditor' con 'default:auditor-sa'.
                                4. Prueba con: 'kubectl auth can-i list deployments --as=system:serviceaccount:default:auditor-sa -A'.
                                """)
                        .officialSolutionEn("""
                                # 1. Create ClusterRole imperatively:
                                kubectl create clusterrole deployment-auditor --verb=get,list,watch --resource=deployments.apps,daemonsets.apps
                                # 2. Create ServiceAccount:
                                kubectl create sa auditor-sa -n default
                                # 3. Create ClusterRoleBinding:
                                kubectl create clusterrolebinding auditor-global-binding --clusterrole=deployment-auditor --serviceaccount=default:auditor-sa
                                # 4. Verify cluster-wide access:
                                kubectl auth can-i list deployments --as=system:serviceaccount:default:auditor-sa -A
                                """)
                        .officialSolutionEs("""
                                # 1. Crear ClusterRole imperativamente:
                                kubectl create clusterrole deployment-auditor --verb=get,list,watch --resource=deployments.apps,daemonsets.apps
                                # 2. Crear ServiceAccount:
                                kubectl create sa auditor-sa -n default
                                # 3. Crear ClusterRoleBinding:
                                kubectl create clusterrolebinding auditor-global-binding --clusterrole=deployment-auditor --serviceaccount=default:auditor-sa
                                # 4. Verificar acceso en todo el clúster:
                                kubectl auth can-i list deployments --as=system:serviceaccount:default:auditor-sa -A
                                """)
                        .yamlSolution("""
                                apiVersion: rbac.authorization.k8s.io/v1
                                kind: ClusterRole
                                metadata:
                                  name: deployment-auditor
                                rules:
                                - apiGroups: ["apps"]
                                  resources: ["deployments", "daemonsets"]
                                  verbs: ["get", "list", "watch"]
                                ---
                                apiVersion: rbac.authorization.k8s.io/v1
                                kind: ClusterRoleBinding
                                metadata:
                                  name: auditor-global-binding
                                subjects:
                                - kind: ServiceAccount
                                  name: auditor-sa
                                  namespace: default
                                roleRef:
                                  kind: ClusterRole
                                  name: deployment-auditor
                                  apiGroup: rbac.authorization.k8s.io
                                """)
                        .fastExamTipEn("Always use 'kubectl create clusterrole <name> --verb=... --resource=...' for fast RBAC setup in CKA.")
                        .fastExamTipEs("Usa siempre 'kubectl create clusterrole' y 'kubectl create clusterrolebinding' de forma imperativa para ahorrar minutos clave.")
                        .verificationCriteria(List.of(
                                "ClusterRole 'deployment-auditor' exists with get/list/watch on apps resources",
                                "ServiceAccount 'auditor-sa' exists in 'default'",
                                "ClusterRoleBinding 'auditor-global-binding' binds SA to ClusterRole",
                                "kubectl auth can-i evaluates to 'yes' across all namespaces"
                        ))
                        .build(),

                CkaExercise.builder()
                        .id("CKA-NET-03")
                        .domain(CkaDomain.SERVICES_AND_NETWORKING)
                        .difficulty(CkaDifficulty.HARD)
                        .weightPoints(8)
                        .timeLimitMinutes(7)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-ingress")
                        .targetResource("app-ingress")
                        .titleEn("Configure Path-Based Ingress Routing with Ingress Resource")
                        .titleEs("Configurar Enrutamiento Ingress Basado en Rutas")
                        .scenarioEn("""
                                Context: Microservices in namespace 'cka-ingress' must be routed via a single HTTP Ingress entrypoint.
                                Task:
                                1. In namespace 'cka-ingress', create an Ingress named 'app-ingress'.
                                2. Route path '/orders' to Service 'order-service' on port 8080.
                                3. Route path '/customers' to Service 'customer-service' on port 8080.
                                4. Set pathType to 'Prefix' for both rules.
                                """)
                        .scenarioEs("""
                                Contexto: Los microservicios en el namespace 'cka-ingress' deben enrutarse a través de un único punto de entrada HTTP Ingress.
                                Tarea:
                                1. En el namespace 'cka-ingress', crea un Ingress llamado 'app-ingress'.
                                2. Enruta el path '/orders' hacia el Service 'order-service' en el puerto 8080.
                                3. Enruta el path '/customers' hacia el Service 'customer-service' en el puerto 8080.
                                4. Configura el pathType como 'Prefix' en ambas reglas.
                                """)
                        .officialSolutionEn("""
                                # Apply the Ingress YAML manifest:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: networking.k8s.io/v1
                                kind: Ingress
                                metadata:
                                  name: app-ingress
                                  namespace: cka-ingress
                                spec:
                                  rules:
                                  - http:
                                      paths:
                                      - path: /orders
                                        pathType: Prefix
                                        backend:
                                          service:
                                            name: order-service
                                            port:
                                              number: 8080
                                      - path: /customers
                                        pathType: Prefix
                                        backend:
                                          service:
                                            name: customer-service
                                            port:
                                              number: 8080
                                EOF
                                """)
                        .officialSolutionEs("""
                                # Aplicar el manifiesto de Ingress:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: networking.k8s.io/v1
                                kind: Ingress
                                metadata:
                                  name: app-ingress
                                  namespace: cka-ingress
                                spec:
                                  rules:
                                  - http:
                                      paths:
                                      - path: /orders
                                        pathType: Prefix
                                        backend:
                                          service:
                                            name: order-service
                                            port:
                                              number: 8080
                                      - path: /customers
                                        pathType: Prefix
                                        backend:
                                          service:
                                            name: customer-service
                                            port:
                                              number: 8080
                                EOF
                                """)
                        .yamlSolution("""
                                apiVersion: networking.k8s.io/v1
                                kind: Ingress
                                metadata:
                                  name: app-ingress
                                  namespace: cka-ingress
                                spec:
                                  rules:
                                  - http:
                                      paths:
                                      - path: /orders
                                        pathType: Prefix
                                        backend:
                                          service:
                                            name: order-service
                                            port:
                                              number: 8080
                                      - path: /customers
                                        pathType: Prefix
                                        backend:
                                          service:
                                            name: customer-service
                                            port:
                                              number: 8080
                                """)
                        .fastExamTipEn("Always verify 'pathType: Prefix' is specified in Ingress v1, otherwise k8s rejects the resource.")
                        .fastExamTipEs("Verifica siempre que 'pathType: Prefix' esté especificado en Ingress v1, de lo contrario la API rechaza el objeto.")
                        .verificationCriteria(List.of(
                                "Ingress 'app-ingress' exists in 'cka-ingress'",
                                "Rule maps /orders to order-service:8080",
                                "Rule maps /customers to customer-service:8080"
                        ))
                        .build(),

                CkaExercise.builder()
                        .id("CKA-SCHED-02")
                        .domain(CkaDomain.WORKLOADS_AND_SCHEDULING)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(7)
                        .timeLimitMinutes(5)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-scheduling")
                        .targetResource("affinity-pod")
                        .titleEn("Configure Pod NodeAffinity with Required Scheduling")
                        .titleEs("Configurar NodeAffinity de Pod con Programación Obligatoria")
                        .scenarioEn("""
                                Context: A production workload must only be scheduled onto nodes with label 'disk=ssd'.
                                Task:
                                1. In namespace 'cka-scheduling', create a pod named 'affinity-pod' running 'nginx:alpine'.
                                2. Configure nodeAffinity under affinity with 'requiredDuringSchedulingIgnoredDuringExecution'.
                                3. Match expression: Key 'disk', Operator 'In', Values ['ssd'].
                                4. (For testing, if node is not labeled, label node minikube: 'kubectl label node minikube disk=ssd --overwrite').
                                5. Verify the pod is scheduled and reaches state 'Running'.
                                """)
                        .scenarioEs("""
                                Contexto: Una carga de producción solo debe ejecutarse en nodos que tengan la etiqueta 'disk=ssd'.
                                Tarea:
                                1. En el namespace 'cka-scheduling', crea un pod llamado 'affinity-pod' con la imagen 'nginx:alpine'.
                                2. Configura nodeAffinity bajo affinity con 'requiredDuringSchedulingIgnoredDuringExecution'.
                                3. Expresión de coincidencia: Key 'disk', Operator 'In', Values ['ssd'].
                                4. (Para probar, si el nodo no tiene la etiqueta, etiqueta minikube: 'kubectl label node minikube disk=ssd --overwrite').
                                5. Verifica que el pod se programe y alcance el estado 'Running'.
                                """)
                        .officialSolutionEn("""
                                # Label node first if needed:
                                kubectl label node minikube disk=ssd --overwrite
                                # Apply pod with NodeAffinity:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: affinity-pod
                                  namespace: cka-scheduling
                                spec:
                                  affinity:
                                    nodeAffinity:
                                      requiredDuringSchedulingIgnoredDuringExecution:
                                        nodeSelectorTerms:
                                        - matchExpressions:
                                          - key: disk
                                            operator: In
                                            values:
                                            - ssd
                                  containers:
                                  - name: web
                                    image: nginx:alpine
                                EOF
                                """)
                        .officialSolutionEs("""
                                # Etiquetar el nodo si hace falta:
                                kubectl label node minikube disk=ssd --overwrite
                                # Aplicar pod con NodeAffinity:
                                cat <<EOF | kubectl apply -f -
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: affinity-pod
                                  namespace: cka-scheduling
                                spec:
                                  affinity:
                                    nodeAffinity:
                                      requiredDuringSchedulingIgnoredDuringExecution:
                                        nodeSelectorTerms:
                                        - matchExpressions:
                                          - key: disk
                                            operator: In
                                            values:
                                            - ssd
                                  containers:
                                  - name: web
                                    image: nginx:alpine
                                EOF
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: affinity-pod
                                  namespace: cka-scheduling
                                spec:
                                  affinity:
                                    nodeAffinity:
                                      requiredDuringSchedulingIgnoredDuringExecution:
                                        nodeSelectorTerms:
                                        - matchExpressions:
                                          - key: disk
                                            operator: In
                                            values:
                                            - ssd
                                  containers:
                                  - name: web
                                    image: nginx:alpine
                                """)
                        .fastExamTipEn("Remember 'requiredDuringScheduling' = hard requirement, while 'preferredDuringScheduling' = soft preference.")
                        .fastExamTipEs("Recuerda: 'requiredDuringScheduling' es requisito estricto (hard), mientras que 'preferredDuringScheduling' es preferencia suave (soft).")
                        .verificationCriteria(List.of(
                                "Pod 'affinity-pod' exists in 'cka-scheduling'",
                                "spec.affinity.nodeAffinity.requiredDuringScheduling... matches disk=ssd",
                                "Pod is in 'Running' phase"
                        ))
                        .build(),

                CkaExercise.builder()
                        .id("CKA-SCHED-03")
                        .domain(CkaDomain.WORKLOADS_AND_SCHEDULING)
                        .difficulty(CkaDifficulty.MEDIUM)
                        .weightPoints(7)
                        .timeLimitMinutes(6)
                        .startingContext("kubectl config use-context minikube")
                        .targetNamespace("cka-workloads")
                        .targetResource("sidecar-app")
                        .titleEn("Deploy Multi-Container Pod with Logging Sidecar Pattern")
                        .titleEs("Desplegar Pod Multi-Contenedor con Patrón Sidecar de Logging")
                        .scenarioEn("""
                                Context: An application container produces logs to a shared volume, and a sidecar container streams them.
                                Task:
                                1. In namespace 'cka-workloads', create a Pod named 'sidecar-app'.
                                2. Container 1 named 'app': runs 'busybox:1.36', writing 'date >> /var/log/app.log; sleep 2' in a loop.
                                3. Container 2 named 'sidecar': runs 'busybox:1.36', streaming with 'tail -n+1 -f /var/log/app.log'.
                                4. Share an 'emptyDir' volume named 'log-volume' mounted at '/var/log' in BOTH containers.
                                5. Verify both containers (2/2) are Ready.
                                """)
                        .scenarioEs("""
                                Contexto: Un contenedor de aplicación escribe logs en un volumen compartido, y un contenedor sidecar los retransmite.
                                Tarea:
                                1. En el namespace 'cka-workloads', crea un Pod llamado 'sidecar-app'.
                                2. Contenedor 1 'app': ejecuta 'busybox:1.36' escribiendo 'date >> /var/log/app.log; sleep 2' en bucle.
                                3. Contenedor 2 'sidecar': ejecuta 'busybox:1.36' transmitiendo con 'tail -n+1 -f /var/log/app.log'.
                                4. Comparte un volumen 'emptyDir' llamado 'log-volume' montado en '/var/log' en AMBOS contenedores.
                                5. Verifica que ambos contenedores (2/2) estén en estado Ready.
                                """)
                        .officialSolutionEn("""
                                cat <<EOF | kubectl apply -f -
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: sidecar-app
                                  namespace: cka-workloads
                                spec:
                                  volumes:
                                  - name: log-volume
                                    emptyDir: {}
                                  containers:
                                  - name: app
                                    image: busybox:1.36
                                    command: ["sh", "-c", "while true; do date >> /var/log/app.log; sleep 2; done"]
                                    volumeMounts:
                                    - name: log-volume
                                      mountPath: /var/log
                                  - name: sidecar
                                    image: busybox:1.36
                                    command: ["sh", "-c", "tail -n+1 -f /var/log/app.log"]
                                    volumeMounts:
                                    - name: log-volume
                                      mountPath: /var/log
                                EOF
                                """)
                        .officialSolutionEs("""
                                cat <<EOF | kubectl apply -f -
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: sidecar-app
                                  namespace: cka-workloads
                                spec:
                                  volumes:
                                  - name: log-volume
                                    emptyDir: {}
                                  containers:
                                  - name: app
                                    image: busybox:1.36
                                    command: ["sh", "-c", "while true; do date >> /var/log/app.log; sleep 2; done"]
                                    volumeMounts:
                                    - name: log-volume
                                      mountPath: /var/log
                                  - name: sidecar
                                    image: busybox:1.36
                                    command: ["sh", "-c", "tail -n+1 -f /var/log/app.log"]
                                    volumeMounts:
                                    - name: log-volume
                                      mountPath: /var/log
                                EOF
                                """)
                        .yamlSolution("""
                                apiVersion: v1
                                kind: Pod
                                metadata:
                                  name: sidecar-app
                                  namespace: cka-workloads
                                spec:
                                  volumes:
                                  - name: log-volume
                                    emptyDir: {}
                                  containers:
                                  - name: app
                                    image: busybox:1.36
                                    command: ["sh", "-c", "while true; do date >> /var/log/app.log; sleep 2; done"]
                                    volumeMounts:
                                    - name: log-volume
                                      mountPath: /var/log
                                  - name: sidecar
                                    image: busybox:1.36
                                    command: ["sh", "-c", "tail -n+1 -f /var/log/app.log"]
                                    volumeMounts:
                                    - name: log-volume
                                      mountPath: /var/log
                                """)
                        .fastExamTipEn("Sidecars in CKA always use a shared emptyDir volume mounted on the same directory across containers.")
                        .fastExamTipEs("Los patrones de sidecar en clústeres de Kubernetes utilizan un volumen compartido emptyDir montado en la misma ruta en ambos contenedores.")
                        .verificationCriteria(List.of(
                                "Pod 'sidecar-app' exists in 'cka-workloads'",
                                "Pod has exactly 2 containers (app and sidecar)",
                                "Both containers mount shared emptyDir volume 'log-volume'",
                                "Both containers are 2/2 Ready"
                        ))
                        .build()
        );
    }
}
