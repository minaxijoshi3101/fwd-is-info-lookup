#! /bin/bash
source ~/.bash_profile
nvm use v16.15.0
npm install --loglevel=error
app_name=$(jq -r '.name' package.json)
version=$(jq -r '.version' package.json)
profile=non_prod_ecr_role
account_id=$2
#touch .env
PARAMETER_STORE_ENV=$(echo $1 | tr '[:lower:]' '[:upper:]')
temp_arr=$(aws ssm get-parameter --name "$1-$app_name" --profile $PARAMETER_STORE_ENV-SG-IS | jq -r .Parameter.Value)

IFS=','     # comma is set as delimiter
read -ra ADDR <<< "$temp_arr"   # str is read into an array as tokens separated by IFS
for i in "${ADDR[@]}"; do   # access each element of array
    echo "$i" >> .env
done

#cat .env | tr -d " " >> env.properties

#file="./env.properties"
#while IFS='=' read -r key value
#do
#    key=$(echo $key | tr '.' '_')
#    eval ${key}=\${value}
#done < "$file"
#
#sed -i "s;%WP_BASE_URL%;$WORDPRESS_BASE_URL;g" app-task-definition.json
#sed -i "s;%I_BASE_URL%;$IS_BASE_URL;g" app-task-definition.json
#sed -i "s;%SALES_PATH%;$CI_SALES_JOURNEY_PATH;g" app-task-definition.json


docker build -t $app_name:$version .
aws ecr get-login-password --region ap-southeast-1 --profile  $profile | docker login --username AWS --password-stdin $account_id.dkr.ecr.ap-southeast-1.amazonaws.com
docker tag $app_name:$version $account_id.dkr.ecr.ap-southeast-1.amazonaws.com/$app_name:$version
docker push $account_id.dkr.ecr.ap-southeast-1.amazonaws.com/$app_name:$version