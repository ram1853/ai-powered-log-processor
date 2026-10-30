# AI Powered Log Processor

AI Powered Log Processor is a backend service that processes application logs, analyzes them using AI, and provides suggestions or possible solutions using a **business knowledge base**.

The system uses **Retrieval Augmented Generation (RAG)** with an **AWS Bedrock Knowledge Base** to correlate logs with known issues and operational knowledge.

Detailed **functional and non-functional requirements** are available in the **GitHub Wiki**.

---

# Architecture Overview

The system processes logs from AWS services, analyzes them using AI, and stores the enriched results for further analysis and operational visibility.

## AWS Services Used

* AWS Lambda – Core processing logic for log analysis
* Amazon CloudWatch – Source of application logs
* Amazon DynamoDB – Stores processed logs along with AI responses
* Amazon Bedrock Knowledge Base – Used for RAG-based AI analysis
* Amazon S3 (General Purpose Bucket) – Stores business knowledge base data
* Amazon S3 (Vector Bucket) – Stores vectorized chunks used by Bedrock
* Amazon API Gateway – APIs used by operations teams for manual intervention
* Amazon Cognito User Pool – Machine-to-machine authentication and authorization using scopes
* AWS IAM – Roles and permissions across services

---

# Technology Stack

## Backend

* Java 17
* Spring Boot 3.5.10
* Maven (dependency management and build)

## Infrastructure

* Terraform (optional, for infrastructure provisioning)
* GitHub Actions (CI/CD pipeline)

---

# Prerequisites

Before setting up the project, ensure the following tools are installed:

* Java 17
* Maven
* Terraform (optional)
* IntelliJ IDEA (recommended for development)

You will also need:

* An **AWS account**
* AWS credentials configured as **repository secrets**

```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

Additionally, **manually create an S3 bucket** that will be used as the **data source for the Bedrock Knowledge Base**.

---

# Project Setup

## 1. Clone the Repository

```bash
git clone git@github.com:ram1853/ai-powered-log-processor.git (or use HTTPS)
cd ai-powered-log-processor
```

## 2. Build the Project

```bash
mvn clean package
```

This will generate the deployable **Lambda JAR artifact**.

---

# Project Structure

```
src/
 ├── main/
 │   └── Java source code
 └── test/
     └── JUnit test cases

operations/
 └── Scripts used to load business data into the knowledge base

infra/
 └── Terraform scripts for infrastructure provisioning

.github/workflows/
 └── CI/CD pipeline definitions
```

---

# Infrastructure Provisioning

Infrastructure for this project can be provisioned using **Terraform** located in the `infra` directory.

The infrastructure includes:

* Lambda functions
* DynamoDB tables
* IAM roles and policies
* Bedrock knowledge base
* Supporting AWS resources

> **Note:**
> The **REST API (API Gateway)** configuration is **not currently included** in the Terraform scripts.
> S3 Bucket has to be globally unique, so replace with your bucket name in variable "s3-bucket-name" in bedrock-knowledge-base.tf

---

# CI/CD Pipeline

The project uses **GitHub Actions** for continuous integration and deployment.

## Workflow

1. Code is pushed to the **main branch**
2. The **CI/CD pipeline is triggered**
3. The pipeline:

   * Builds the Maven project
   * Packages the Lambda artifact
   * Provisions or updates infrastructure using Terraform

---

# Knowledge Base Data

The **business knowledge base** used for AI analysis is stored in **Amazon S3**.

Scripts in the `operations` directory are used to:

* Upload business data
* Trigger ingestion into the Bedrock Knowledge Base
