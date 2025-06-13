#!/bin/bash

# ============================================================================
# TERRAFORM DEPLOYMENT SCRIPT - ECOMMERCE MICROSERVICES
# ============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if environment argument is provided
if [ $# -eq 0 ]; then
    print_error "No environment specified!"
    echo "Usage: $0 <environment> [action]"
    echo "Environments: dev, stage, prod"
    echo "Actions: plan, apply, destroy, output"
    echo "Example: $0 dev plan"
    exit 1
fi

ENVIRONMENT=$1
ACTION=${2:-plan}

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|stage|prod)$ ]]; then
    print_error "Invalid environment: $ENVIRONMENT"
    echo "Valid environments: dev, stage, prod"
    exit 1
fi

# Set working directory
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_DIR="$TERRAFORM_DIR/environments/$ENVIRONMENT"

print_status "🚀 Starting Terraform deployment for environment: $ENVIRONMENT"
print_status "📁 Working directory: $ENV_DIR"

# Check if environment directory exists
if [ ! -d "$ENV_DIR" ]; then
    print_error "Environment directory not found: $ENV_DIR"
    exit 1
fi

# Navigate to environment directory
cd "$ENV_DIR"

# Check if Terraform is installed
if ! command -v terraform &> /dev/null; then
    print_error "Terraform is not installed or not in PATH"
    exit 1
fi

# Check if kubectl is available and cluster is accessible
if ! command -v kubectl &> /dev/null; then
    print_warning "kubectl is not installed. Make sure Kubernetes cluster is accessible."
fi

# Initialize Terraform
print_status "🔧 Initializing Terraform..."
terraform init -backend-config="backend-${ENVIRONMENT}.hcl" || {
    print_warning "Backend config not found, using local state"
    terraform init
}

# Validate Terraform configuration
print_status "✅ Validating Terraform configuration..."
terraform validate

# Execute the requested action
case $ACTION in
    "plan")
        print_status "📋 Creating Terraform execution plan..."
        terraform plan -var-file="terraform.tfvars" -out="tfplan-${ENVIRONMENT}"
        print_success "Plan created successfully! Review the changes above."
        echo ""
        echo "To apply these changes, run:"
        echo "  $0 $ENVIRONMENT apply"
        ;;
    
    "apply")
        if [ -f "tfplan-${ENVIRONMENT}" ]; then
            print_status "🚀 Applying Terraform plan..."
            terraform apply "tfplan-${ENVIRONMENT}"
        else
            print_status "🚀 Applying Terraform configuration..."
            terraform apply -var-file="terraform.tfvars" -auto-approve
        fi
        print_success "Infrastructure deployed successfully!"
        
        # Show outputs
        print_status "📊 Deployment outputs:"
        terraform output
        ;;
    
    "destroy")
        print_warning "⚠️  This will DESTROY all infrastructure in the $ENVIRONMENT environment!"
        read -p "Are you sure? Type 'yes' to confirm: " -r
        if [[ $REPLY == "yes" ]]; then
            print_status "💥 Destroying infrastructure..."
            terraform destroy -var-file="terraform.tfvars" -auto-approve
            print_success "Infrastructure destroyed successfully!"
        else
            print_status "Destroy cancelled."
        fi
        ;;
    
    "output")
        print_status "📊 Terraform outputs:"
        terraform output
        ;;
    
    *)
        print_error "Invalid action: $ACTION"
        echo "Valid actions: plan, apply, destroy, output"
        exit 1
        ;;
esac

print_success "✨ Terraform $ACTION completed for environment: $ENVIRONMENT" 