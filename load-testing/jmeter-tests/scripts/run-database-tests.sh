#!/bin/bash

echo "=== Starting Database Performance Tests ==="
echo "Test started at: $(date)"

mkdir -p ../results

echo "Executing database performance test..."
/opt/jmeter/bin/jmeter -n -t ../database-tests/db-performance-test.jmx \
    -l ../results/database-performance-results.jtl \
    -e -o ../results/database-performance-report

if [ $? -eq 0 ]; then
    echo "Database performance test completed successfully!"
    echo "Results saved to: ../results/database-performance-results.jtl"
    echo "HTML report generated at: ../results/database-performance-report"
else
    echo "Database performance test failed!"
    exit 1
fi

echo "Test completed at: $(date)"
