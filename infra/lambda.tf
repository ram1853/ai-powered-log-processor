variable "function_name" {
  type    = string
  default = "log-processor"
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

data "aws_iam_policy_document" "assume_role" {
    statement {
      effect = "Allow"

      principals {
        type        = "Service"
        identifiers = ["lambda.amazonaws.com"]
      }

      actions = ["sts:AssumeRole"]
    }
}

resource "aws_iam_policy" "cloudwatch-policy" {
  name = "cloudwatch-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
        {
            Action  = ["logs:CreateLogStream", "logs:PutLogEvents"]
            Effect   = "Allow"
            Resource = "arn:aws:logs:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${var.function_name}:*"
        },
        {
            Action  = ["logs:CreateLogGroup"]
            Effect   = "Allow"
            Resource = "arn:aws:logs:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:*"
        }
    ]
  })
}

resource "aws_iam_policy" "bedrock-knowledge-base-policy" {
  name = "bedrock-knowledge-base-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
        {
            Action  = ["bedrock:RetrieveAndGenerate", "bedrock:InvokeModel", "bedrock:Retrieve"]
            Effect   = "Allow"
            Resource = "*"
        }
    ]
  })
}

resource "aws_iam_policy" "dynamodb-policy" {
  name = "dynamodb-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
        {
            Action  = ["dynamodb:BatchWriteItem", "dynamodb:DescribeTable"]
            Effect   = "Allow"
            Resource = "arn:aws:dynamodb:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:table/ErrorLog"
        }
    ]
  })
}

# IAM Role that will be assumed by Lambda
resource "aws_iam_role" "log-processor-role" {
  name               = "log-processor-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

resource "aws_iam_role_policy_attachment" "attach-cloudwatch-policy" {
  role       = aws_iam_role.log-processor-role.name
  policy_arn = aws_iam_policy.cloudwatch-policy.arn
}

resource "aws_iam_role_policy_attachment" "attach-bedrock-knowledge-base-policy" {
  role       = aws_iam_role.log-processor-role.name
  policy_arn = aws_iam_policy.bedrock-knowledge-base-policy.arn
}

resource "aws_iam_role_policy_attachment" "attach-dynamodb-policy" {
  role       = aws_iam_role.log-processor-role.name
  policy_arn = aws_iam_policy.dynamodb-policy.arn
}

resource "aws_lambda_permission" "allow_cloudwatch_logs_invoke" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.log-processor-tf.function_name
  principal     = "logs.amazonaws.com"
  source_arn    = "arn:aws:logs:ap-south-1:545009866715:log-group:log-events:*"
}

resource "aws_lambda_function" "log-processor-tf" {
  function_name     = var.function_name
  role              = aws_iam_role.log-processor-role.arn
  runtime           = "java17"
  handler           = "org.springframework.cloud.function.adapter.aws.FunctionInvoker"
  timeout           = 60
  environment {
    variables = {
        logging_level_com_log_processor = "INFO"
        spring_cloud_function_definition = "cloudWatchHandler|aiProcessor"
    }
  }
  filename         = "${path.module}/../target/log-processor-0.0.1-SNAPSHOT-aws.jar"
  source_code_hash = filebase64sha256("${path.module}/../target/log-processor-0.0.1-SNAPSHOT-aws.jar") 
}