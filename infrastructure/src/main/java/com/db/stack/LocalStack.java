package com.db.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;
import software.amazon.awscdk.services.servicediscovery.DnsRecordType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalStack extends Stack {

    private final Vpc vpc;
    private final Cluster ecsCluster;

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.vpc = createVpc();

        DatabaseInstance authServiceDb = createDatabaseInstance("AuthServiceDB", "auth-service-db");
        DatabaseInstance patientServiceDb = createDatabaseInstance("PatientServiceDB", "patient-service-db");

        CfnHealthCheck authServiceDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");
        CfnHealthCheck patientServiceDbHealthCheck = createDbHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");

        CfnCluster mskCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();

        FargateService authService = createFargateService(
                "AuthService",
                "auth-service",
                List.of(4005),
                authServiceDb,
                Map.of("JWT_SECRET", "nQO8R0Wc9fLhZ5NkJzG/1wZ+vTCpLE4JkEt3zD4K1bY="));
        authService.getNode().addDependency(authServiceDbHealthCheck);
        authService.getNode().addDependency(authServiceDb);

        FargateService billingService = createFargateService(
                "BillingService",
                "billing-service",
                List.of(4001, 9001),
                null,
                null);

        FargateService analyticsService = createFargateService(
                "AnalyticsService",
                "analytics-service",
                List.of(4002),
                null,
                null
        );

        analyticsService.getNode().addDependency(mskCluster);

        FargateService patientService = createFargateService(
                "PatientService",
                "patient-service",
                List.of(4000),
                patientServiceDb,
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "billing-service.patient-management.local",
                        "BILLING_SERVICE_GRPC_PORT", "9001"
                ));
        patientService.getNode().addDependency(patientServiceDbHealthCheck);
        patientService.getNode().addDependency(patientServiceDb);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskCluster);

        createApiGatewayService();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "PatientManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions
                        .builder()
                        .name("patient-management.local")
                        .build())
                .build();
    }

    private Vpc createVpc() {
        return Vpc.Builder
                .create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    private DatabaseInstance createDatabaseInstance(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine
                        .postgres(PostgresInstanceEngineProps
                                .builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()))
                .vpc(vpc)
                .instanceType(InstanceType
                        .of(InstanceClass.BURSTABLE2,
                                InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(CfnHealthCheck
                        .HealthCheckConfigProperty
                        .builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster createMskCluster() {
        int brokerCount = vpc.getAvailabilityZones().size();
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.11.1")
                .numberOfBrokerNodes(brokerCount)
                .brokerNodeGroupInfo(CfnCluster
                        .BrokerNodeGroupInfoProperty
                        .builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc
                                .getPrivateSubnets()
                                .stream()
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList()))
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }

    private FargateService createFargateService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> envVars) {

        FargateTaskDefinition taskDefinition = FargateTaskDefinition
                .Builder
                .create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions.Builder containerOptions = ContainerDefinitionOptions
                .builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports
                        .stream()
                        .map(port -> PortMapping
                                .builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver
                        .awsLogs(AwsLogDriverProps
                                .builder()
                                .logGroup(LogGroup
                                        .Builder
                                        .create(this, id + "LogGroup")
                                        .logGroupName("/ecs/" + imageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix(imageName)
                                .build()));

        Map<String, String> containerEnvVars = new HashMap<>();
        containerEnvVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");
        if (envVars != null) {
            containerEnvVars.putAll(envVars);
        }

        if (db != null) {
            containerEnvVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName
            ));
            containerEnvVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            containerEnvVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
            containerEnvVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            containerEnvVars.put("SPRING_SQL_INIT_MODE", "always");
            containerEnvVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        containerOptions.environment(containerEnvVars);
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        return FargateService
                .Builder
                .create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .cloudMapOptions(CloudMapOptions.builder()
                        .name(imageName)
                        .dnsRecordType(DnsRecordType.A)
                        .build())
                .serviceName(imageName)
                .build();
    }

    private void createApiGatewayService() {
        String apiGatewayImageName = "api-gateway";
        FargateTaskDefinition taskDefinition = FargateTaskDefinition
                .Builder
                .create(this, "APIGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions
                .builder()
                .image(ContainerImage.fromRegistry(apiGatewayImageName))
                .environment(Map.of(
                        "SPRING_PROFILES_ACTIVE", "prod",
                        "AUTH_SERVICE_URL", "http://auth-service.patient-management.local:4005"
                ))
                .portMappings(List.of(4004)
                        .stream()
                        .map(port -> PortMapping
                                .builder()
                                .containerPort(port)
                                .hostPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver
                        .awsLogs(AwsLogDriverProps
                                .builder()
                                .logGroup(LogGroup
                                        .Builder
                                        .create(this, "ApiGatewayLogGroup")
                                        .logGroupName("/ecs/" + apiGatewayImageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix(apiGatewayImageName)
                                .build()))
                .build();

        taskDefinition.addContainer("APIGatewayContainer", containerOptions);

        ApplicationLoadBalancedFargateService apiGateway
                = ApplicationLoadBalancedFargateService.Builder.create(this, "APIGatewayService")
                .cluster(ecsCluster)
                .serviceName(apiGatewayImageName)
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .healthCheckGracePeriod(Duration.seconds(60))
                .cloudMapOptions(CloudMapOptions
                        .builder()
                        .name(apiGatewayImageName)
                        .dnsRecordType(DnsRecordType.A)
                        .build())
                .build();

    }

    public static void main(final String[] args) {
        final App app = new App(AppProps.builder().outdir("./cdk.out").build());
        StackProps props = StackProps
                .builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();
        new LocalStack(app, "localstack", props);
        app.synth();
        System.out.println("cdk synth successful");
    }

}
