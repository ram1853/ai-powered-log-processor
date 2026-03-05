data "aws_iam_policy_document" "assume_role_for_knowledge_base" {
    statement {
      effect = "Allow"

      principals {
        type        = "Service"
        identifiers = ["bedrock.amazonaws.com"]
      }

      actions = ["sts:AssumeRole"]

      condition {
        test = "StringEquals"
        variable = "aws:SourceAccount"
        values = [ data.aws_caller_identity.current.account_id ]
      }

      condition {
        test = "ArnLike"
        variable = "aws:SourceArn"
        values = [ "arn:aws:bedrock:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:knowledge-base/*" ]
      }
    }
}

resource "aws_iam_policy" "foundation-model-policy" {
  name = "foundation-model-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
        {
            Action  = ["bedrock:InvokeModel"]
            Effect   = "Allow"
            Resource = "arn:aws:bedrock:${data.aws_region.current.region}::foundation-model/amazon.titan-embed-text-v2:0"
        },
        {
            Action    = ["aws-marketplace:Subscribe", "aws-marketplace:ViewSubscriptions", "aws-marketplace:Unsubscribe"]
            Effect    = "Allow"
            Resource  = "*"
            Condition = {
                StringEquals = {
                  "aws:CalledViaLast" = "bedrock.amazonaws.com"
              }
            }
        }
    ]
  })
}

resource "aws_iam_policy" "s3-policy" {
  name = "s3-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
        {
            Action  = ["s3:ListBucket"]
            Effect   = "Allow"
            Resource = ["arn:aws:s3:::log-processor-knowledge-base"]
            Condition = {
                StringEquals = {
                  "aws:ResourceAccount" = "${data.aws_caller_identity.current.account_id}"
              }
            }
        },
        {
            Action    = ["s3:GetObject"]
            Effect    = "Allow"
            Resource  = ["arn:aws:s3:::log-processor-knowledge-base/*"]
            Condition = {
                StringEquals = {
                  "aws:ResourceAccount" = "${data.aws_caller_identity.current.account_id}"
              }
            }
        }
    ]
  })
}

resource "aws_iam_policy" "vector-store-policy" {
  name = "vector-store-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
        {
            Action  = ["s3vectors:GetIndex", "s3vectors:QueryVectors", "s3vectors:PutVectors", "s3vectors:GetVectors", "s3vectors:DeleteVectors"]
            Effect   = "Allow"
            Resource = ["arn:aws:s3vectors:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:bucket/${aws_s3vectors_vector_bucket.s3-vector-bucket.vector_bucket_name}/index/${aws_s3vectors_index.s3-vector-bucket-index.index_name}"]
            Condition = {
                StringEquals = {
                  "aws:ResourceAccount" = "${data.aws_caller_identity.current.account_id}"
              }
            }
        }
    ]
  })
}

resource "aws_iam_role" "knowledge-base-role" {
  name               = "knowledge-base-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role_for_knowledge_base.json
}

resource "aws_iam_role_policy_attachment" "attach-foundation-model-policy" {
  role       = aws_iam_role.knowledge-base-role.name
  policy_arn = aws_iam_policy.foundation-model-policy.arn
}

resource "aws_iam_role_policy_attachment" "attach-s3-policy" {
  role       = aws_iam_role.knowledge-base-role.name
  policy_arn = aws_iam_policy.s3-policy.arn
}

resource "aws_iam_role_policy_attachment" "attach-vector-store-policy" {
  role       = aws_iam_role.knowledge-base-role.name
  policy_arn = aws_iam_policy.vector-store-policy.arn
}

resource "aws_s3vectors_vector_bucket" "s3-vector-bucket" {
  vector_bucket_name = "log-processor-tf-vector"
}

resource "aws_s3vectors_index" "s3-vector-bucket-index" {
  index_name         = "default-index"
  vector_bucket_name = aws_s3vectors_vector_bucket.s3-vector-bucket.vector_bucket_name

  data_type       = "float32"
  dimension       = 1024
  distance_metric = "euclidean"

  metadata_configuration {
    non_filterable_metadata_keys = ["AMAZON_BEDROCK_TEXT", "AMAZON_BEDROCK_METADATA"]
  }
}

resource "aws_bedrockagent_knowledge_base" "log-processor-knowledge-base-tf" {
  name     = "log-processor-knowledge-base-tf"
  role_arn = aws_iam_role.knowledge-base-role.arn
  knowledge_base_configuration {
    vector_knowledge_base_configuration {
      embedding_model_arn = "arn:aws:bedrock:${data.aws_region.current.region}::foundation-model/amazon.titan-embed-text-v2:0"
      
      embedding_model_configuration {
        bedrock_embedding_model_configuration {
          dimensions          = 1024
          embedding_data_type = "FLOAT32"
        }
      }
    }
    type = "VECTOR"
  }
  storage_configuration {
    type = "S3_VECTORS"
    s3_vectors_configuration {
      index_arn = aws_s3vectors_index.s3-vector-bucket-index.index_arn
    }
  }
}

resource "aws_bedrockagent_data_source" "data-source" {
  knowledge_base_id = aws_bedrockagent_knowledge_base.log-processor-knowledge-base-tf.id
  name              = "knowledge-base-data-source"
  data_source_configuration {
    type = "S3"
    s3_configuration {
      bucket_arn = "arn:aws:s3:::log-processor-knowledge-base"
    }
  }
}