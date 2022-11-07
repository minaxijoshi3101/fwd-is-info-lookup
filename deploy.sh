#To-do deployment instructions
#! /bin/bash
app_name=$(jq -r '.name' package.json)
version=$(jq -r '.version' package.json)
profile=$1-SG-IS
cluster_name=$profile
account_id=$2
shared_svcs_account_id=$3
if [[ "$1" == "SIT" ]]; then
    targetGroupARN=arn:aws:elasticloadbalancing:ap-southeast-1:165901213126:targetgroup/fwd-is-info-lookup/14bf9964b1002685
    networkConfig="awsvpcConfiguration={subnets=[subnet-000afa848a09d7893,subnet-00a7d691241b6fb66],securityGroups=[sg-0fb000da57a1a0e9a]}"
else
    echo "Wrong Environment"
fi
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
sed -i "s;%CONTAINER_PORT%;8080;g" app-task-definition.json
sed -i "s;%HOST_PORT%;8080;g" app-task-definition.json
# COMMANDS to register task definitions 
aws ecs register-task-definition --cli-input-json --profile $profile file://app-task-definition.json

aws ecs create-service --cluster $cluster_name --service-name $app_name --task-definition $app_name --load-balancers targetGroupArn=$targetGroupARN,containerName=$app_name,containerPort=8080 --desired-count 1 --launch-type "FARGATE" --network-configuration $networkConfig --profile $profile | error=true
aws ecs update-service --cluster $cluster_name --service $app_name --task-definition $app_name --desired-count 1 --profile $profile