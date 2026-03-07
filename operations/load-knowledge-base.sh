#!/bin/zsh

DIRECTORY=$1
S3_BUCKET=$2

KNOWLEDGE_BASE_ID=$3
DATA_SOURCE_ID=$4
REGION=$5

aws s3 sync $DIRECTORY s3://$S3_BUCKET

aws bedrock-agent start-ingestion-job --knowledge-base-id $KNOWLEDGE_BASE_ID \
 --data-source-id $DATA_SOURCE_ID \
 --region $REGION