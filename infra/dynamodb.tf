resource "aws_dynamodb_table" "ErrorLog-dynamodb-table" {
  name           = "ErrorLog"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "eventId"
  range_key      = "ingestionTime"

  attribute {
    name = "eventId"
    type = "S"
  }

  attribute {
    name = "ingestionTime"
    type = "N"
  }
}