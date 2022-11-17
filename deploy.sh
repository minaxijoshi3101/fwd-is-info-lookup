#To-do deployment instructions
#! /bin/bash
app_name=$(xmllint --xpath '/*[local-name()="project"]/*[local-name()="artifactId"]/text()' pom.xml)
version=$(xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml)
profile=$1-SG-IS
cluster_name=$profile
account_id=$2
shared_svcs_account_id=$3
if [[ "$1" == "SIT" ]]; then
    targetGroupARN=arn:aws:elasticloadbalancing:ap-southeast-1:165901213126:targetgroup/fwd-is-info-lookup/14bf9964b1002685
    networkConfig="awsvpcConfiguration={subnets=[subnet-000afa848a09d7893,subnet-00a7d691241b6fb66],securityGroups=[sg-0fb000da57a1a0e9a]}"
    AWS_SECRET_MANAGER_ENABLED=true
    AWS_SECRET_MANAGER_ARN=arn:aws:secretsmanager:ap-southeast-1:165901213126:secret:sit-fwd-is-info-lookup-M60r5f
    elif [[ "$1" == "UAT" ]]; then
    targetGroupARN=arn:aws:elasticloadbalancing:ap-southeast-1:022525172765:targetgroup/fwd-is-info-lookup/a7c4eba721afa70c
    networkConfig='awsvpcConfiguration={subnets=[subnet-0dadf4921a5da0251,subnet-0ebd7a195c050f2b8],securityGroups=[sg-0e24c8e15bfbc37a1]}'
    AWS_SECRET_MANAGER_ENABLED=true
    AWS_SECRET_MANAGER_ARN=arn:aws:secretsmanager:ap-southeast-1:022525172765:secret:uat-fwd-is-info-lookup-ETiDYa
elif [[ "$1" == "PRE-PROD" ]]; then
    targetGroupARN=arn:aws:elasticloadbalancing:ap-southeast-1:230108722688:targetgroup/fwd-is-info-lookup/0ad59f985638f66b
    networkConfig='awsvpcConfiguration={subnets=[subnet-08c2805b2d5c482f9,subnet-06649d60a6c470246],securityGroups=[sg-04ae1fe1a7935713f]}'
    AWS_SECRET_MANAGER_ENABLED=true
    AWS_SECRET_MANAGER_ARN=arn:aws:secretsmanager:ap-southeast-1:230108722688:secret:pre-prod-fwd-is-info-lookup-LuN0uX
elif [[ "$1" == "PROD" ]]; then
    echo "No resources for PROD now, pending creation" 
    else
    echo "Wrong Environment"
    fi
    PARAMETER_STORE_ENV=$(echo $1 | tr '[:upper:]' '[:lower:]')
    temp_arr=$(aws ssm get-parameter --name "$PARAMETER_STORE_ENV-$app_name" --profile $1-SG-IS | jq -r .Parameter.Value)
    IFS=','     # comma is set as delimiter
    read -ra ADDR <<< "$temp_arr"   # str is read into an array as tokens separated by IFS
    for i in "${ADDR[@]}"; do   # access each element of array
        echo "$i" >> .env
    done

cat .env | tr -d " " >> env.properties

file="./env.properties"
while IFS='=' read -r key value
do
    key=$(echo $key | tr '.' '_')
    eval ${key}=\${value}
done < "$file"

sed -i "s;%WP_BASE_URL%;$WORDPRESS_BASE_URL;g" app-task-definition.json
sed -i "s;%I_BASE_URL%;$IS_BASE_URL;g" app-task-definition.json
sed -i "s;%APP_NAME%;$app_name;g" app-task-definition.json
sed -i "s;%VERSION%;$version;g" app-task-definition.json
sed -i "s;%ACCOUNT_ID%;$account_id;g" app-task-definition.json
sed -i "s;%SHARED_SVCS_ACCOUNT_ID%;$shared_svcs_account_id;g" app-task-definition.json
sed -i "s;%CONTAINER_PORT%;5002;g" app-task-definition.json
sed -i "s;%HOST_PORT%;5002;g" app-task-definition.json
sed -i "s;%AWS_SECRET_MANAGER_ENABLED%;$AWS_SECRET_MANAGER_ENABLED;g" app-task-definition.json
sed -i "s;%AWS_SECRET_MANAGER_ARN%;$AWS_SECRET_MANAGER_ARN;g" app-task-definition.json
# COMMANDS to register task definitions 
aws ecs register-task-definition --cli-input-json --profile $profile file://app-task-definition.json

aws ecs create-service --cluster $cluster_name --service-name $app_name --task-definition $app_name --load-balancers targetGroupArn=$targetGroupARN,containerName=$app_name,containerPort=5002 --desired-count 1 --launch-type "FARGATE" --network-configuration "$networkConfig" --profile $profile | error=true
aws ecs update-service --cluster $cluster_name --service $app_name --task-definition $app_name --desired-count 1 --profile $profile