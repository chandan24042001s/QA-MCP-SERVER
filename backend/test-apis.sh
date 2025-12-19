#!/bin/bash

# API Testing Script for QA MCP Server
# This script tests all backend API endpoints

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "QA MCP Server API Testing"
echo "=========================================="
echo ""

# Check if server is running
echo -e "${YELLOW}Checking if server is running...${NC}"
if curl -s -f "$BASE_URL/.well-known/mcp/manifest" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}"
else
    echo -e "${RED}✗ Server is not running. Please start the backend server first.${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo "Testing Endpoints"
echo "=========================================="
echo ""

# Test 1: Manifest
echo -e "${YELLOW}Test 1: Manifest${NC}"
response=$(curl -s "$BASE_URL/.well-known/mcp/manifest")
if echo "$response" | grep -q "qa-mcp-server"; then
    echo -e "${GREEN}✓ Manifest endpoint working${NC}"
else
    echo -e "${RED}✗ Manifest endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 2: AI Code Insights (with path)
echo -e "${YELLOW}Test 2: AI Code Insights (local path)${NC}"
response=$(curl -s -X POST "$BASE_URL/call/ai/code_insights" \
  -H "Content-Type: application/json" \
  -d '{"args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Code Insights endpoint responding${NC}"
    echo "Response preview: $(echo "$response" | head -c 200)..."
else
    echo -e "${RED}✗ Code Insights endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 3: Defect Prediction
echo -e "${YELLOW}Test 3: Defect Prediction${NC}"
response=$(curl -s -X POST "$BASE_URL/call/ai/defect_prediction" \
  -H "Content-Type: application/json" \
  -d '{"args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Defect Prediction endpoint responding${NC}"
else
    echo -e "${RED}✗ Defect Prediction endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 4: Test Gap Analysis
echo -e "${YELLOW}Test 4: Test Gap Analysis${NC}"
response=$(curl -s -X POST "$BASE_URL/call/ai/test_gap_analysis" \
  -H "Content-Type: application/json" \
  -d '{"args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Test Gap Analysis endpoint responding${NC}"
else
    echo -e "${RED}✗ Test Gap Analysis endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 5: Refactor Advisor
echo -e "${YELLOW}Test 5: Refactor Advisor${NC}"
response=$(curl -s -X POST "$BASE_URL/call/ai/refactor_advisor" \
  -H "Content-Type: application/json" \
  -d '{"args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Refactor Advisor endpoint responding${NC}"
else
    echo -e "${RED}✗ Refactor Advisor endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 6: Memory Leak Prediction
echo -e "${YELLOW}Test 6: Memory Leak Prediction${NC}"
response=$(curl -s -X POST "$BASE_URL/call/ai/memory_leak_prediction" \
  -H "Content-Type: application/json" \
  -d '{"args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Memory Leak Prediction endpoint responding${NC}"
else
    echo -e "${RED}✗ Memory Leak Prediction endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 7: Scan Repository
echo -e "${YELLOW}Test 7: Scan Repository${NC}"
response=$(curl -s -X POST "$BASE_URL/call/scan/repository" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"test-123","args":{"repoUrl":"https://github.com/chandan24042001s/GPT-Powered-NetFlix.git","branch":"main"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Scan Repository endpoint responding${NC}"
else
    echo -e "${RED}✗ Scan Repository endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 8: Scan Files
echo -e "${YELLOW}Test 8: Scan Files${NC}"
response=$(curl -s -X POST "$BASE_URL/call/scan/files" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"test-123","args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Scan Files endpoint responding${NC}"
else
    echo -e "${RED}✗ Scan Files endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 9: Run Tests
echo -e "${YELLOW}Test 9: Run Tests${NC}"
response=$(curl -s -X POST "$BASE_URL/call/test/run" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"test-123","args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Run Tests endpoint responding${NC}"
else
    echo -e "${RED}✗ Run Tests endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

# Test 10: Tech Debt Report
echo -e "${YELLOW}Test 10: Tech Debt Report${NC}"
response=$(curl -s -X POST "$BASE_URL/call/report/tech-debt" \
  -H "Content-Type: application/json" \
  -d '{"requestId":"test-123","args":{"path":"/tmp"}}')
if echo "$response" | grep -q "status"; then
    echo -e "${GREEN}✓ Tech Debt Report endpoint responding${NC}"
else
    echo -e "${RED}✗ Tech Debt Report endpoint failed${NC}"
    echo "Response: $response"
fi
echo ""

echo "=========================================="
echo "Testing Complete"
echo "=========================================="

