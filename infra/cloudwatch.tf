locals {
    cloudwatch_log_groups = {
    "log-events" = "log-events-error-filter"
    }
}

resource "aws_cloudwatch_log_subscription_filter" "log_processor_lambda_logfilter" {
  for_each        = local.cloudwatch_log_groups
  name            = each.value
  log_group_name  = each.key
  filter_pattern  = "\"Level: Error\""
  destination_arn = aws_lambda_function.log-processor.arn
}