#!/bin/bash

set -e # Stops the script of any command fails

# Set dummy AWS credentials for LocalStack
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1

# Delete old one
#aws --endpoint-url=http://localstack:4566 cloudformation delete-stack \
#    --stack-name patient-management

# Deploy CloudFormation stack
aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name patient-management \
    --template-file "./cdk.out/localstack.template.json"

# Get the load balancer DNS name
aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text