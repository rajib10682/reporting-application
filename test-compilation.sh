#!/bin/bash

echo "Testing Spring Boot microservices compilation..."

echo "Building report-service..."
cd backend/report-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✓ report-service compiled successfully"
else
    echo "✗ report-service compilation failed"
    exit 1
fi
cd ../..

echo "Building user-service..."
cd backend/user-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✓ user-service compiled successfully"
else
    echo "✗ user-service compilation failed"
    exit 1
fi
cd ../..

echo "Building data-service..."
cd backend/data-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✓ data-service compiled successfully"
else
    echo "✗ data-service compilation failed"
    exit 1
fi
cd ../..

echo "Building gateway-service..."
cd backend/gateway-service
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "✓ gateway-service compiled successfully"
else
    echo "✗ gateway-service compilation failed"
    exit 1
fi
cd ../..

echo "Testing Angular frontend compilation..."
cd frontend
npm run build --prod=false 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✓ Angular frontend compiled successfully"
else
    echo "✗ Angular frontend compilation failed"
    exit 1
fi
cd ..

echo "All services compiled successfully!"
