# ============================================================================
# TERRAFORM REMOTE BACKEND CONFIGURATION
# ============================================================================

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ============================================================================
# S3 BUCKET FOR TERRAFORM STATE
# ============================================================================

resource "aws_s3_bucket" "terraform_state" {
  bucket = "ecommerce-terraform-state-${var.environment}-${random_string.bucket_suffix.result}"

  tags = {
    Name        = "Terraform State"
    Environment = var.environment
    Project     = "ecommerce-microservices"
    ManagedBy   = "terraform"
  }
}

resource "random_string" "bucket_suffix" {
  length  = 8
  special = false
  upper   = false
}

resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_encryption" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ============================================================================
# DYNAMODB TABLE FOR STATE LOCKING
# ============================================================================

resource "aws_dynamodb_table" "terraform_locks" {
  name           = "terraform-locks-${var.environment}"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name        = "Terraform Locks"
    Environment = var.environment
    Project     = "ecommerce-microservices"
    ManagedBy   = "terraform"
  }
}

# ============================================================================
# OUTPUTS
# ============================================================================

output "s3_bucket_name" {
  description = "Name of the S3 bucket for Terraform state"
  value       = aws_s3_bucket.terraform_state.bucket
}

output "dynamodb_table_name" {
  description = "Name of the DynamoDB table for state locking"
  value       = aws_dynamodb_table.terraform_locks.name
}

output "backend_config" {
  description = "Backend configuration for Terraform"
  value = {
    bucket         = aws_s3_bucket.terraform_state.bucket
    key            = "ecommerce/${var.environment}/terraform.tfstate"
    region         = var.aws_region
    dynamodb_table = aws_dynamodb_table.terraform_locks.name
    encrypt        = true
  }
} 