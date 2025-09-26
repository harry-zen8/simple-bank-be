#!/bin/bash

# Simple Bank Test Runner Script
# This script provides easy commands to run different types of tests

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

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  all                 Run all tests"
    echo "  unit                Run unit tests only"
    echo "  bdd                 Run BDD integration tests only"
    echo "  api                 Run API integration tests only"
    echo "  e2e                 Run end-to-end tests only"
    echo "  integration         Run all integration tests"
    echo "  coverage            Run all tests with coverage report"
    echo "  clean               Clean and run all tests"
    echo "  help                Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 all              # Run all tests"
    echo "  $0 unit             # Run only unit tests"
    echo "  $0 coverage         # Run tests with coverage report"
}

# Function to run unit tests
run_unit_tests() {
    print_status "Running unit tests..."
    mvn test -Dtest="*ServiceTest" -q
    print_success "Unit tests completed"
}

# Function to run BDD tests
run_bdd_tests() {
    print_status "Running BDD integration tests..."
    mvn test -Dtest=CucumberTestRunner -q
    print_success "BDD tests completed"
}

# Function to run API integration tests
run_api_tests() {
    print_status "Running API integration tests..."
    mvn test -Dtest="*ControllerIntegrationTest" -q
    print_success "API integration tests completed"
}


# Function to run E2E tests
run_e2e_tests() {
    print_status "Running end-to-end tests..."
    mvn test -Dtest="*E2ETest" -q
    print_success "End-to-end tests completed"
}

# Function to run all integration tests
run_integration_tests() {
    print_status "Running all integration tests..."
    mvn test -Dtest=IntegrationTestSuite -q
    print_success "Integration tests completed"
}

# Function to run all tests
run_all_tests() {
    print_status "Running all tests..."
    mvn test -q
    print_success "All tests completed"
}

# Function to run tests with coverage
run_coverage_tests() {
    print_status "Running tests with coverage report..."
    mvn clean test jacoco:report -q
    print_success "Tests completed with coverage report"
    print_status "Coverage report available at: target/site/jacoco/index.html"
}

# Function to clean and run tests
run_clean_tests() {
    print_status "Cleaning and running all tests..."
    mvn clean test -q
    print_success "Clean test run completed"
}

# Main script logic
case "${1:-help}" in
    "all")
        run_all_tests
        ;;
    "unit")
        run_unit_tests
        ;;
    "bdd")
        run_bdd_tests
        ;;
    "api")
        run_api_tests
        ;;
    "e2e")
        run_e2e_tests
        ;;
    "integration")
        run_integration_tests
        ;;
    "coverage")
        run_coverage_tests
        ;;
    "clean")
        run_clean_tests
        ;;
    "help"|*)
        show_usage
        ;;
esac
