#!/bin/bash

echo "=== Starting Redis Queue Performance Tests ==="
echo "Test started at: $(date)"

mkdir -p ../results

echo "Executing Redis queue performance test..."
/opt/jmeter/bin/jmeter -n -t ../redis-tests/redis-queue-test.jmx \
    -l ../results/redis-queue-results.jtl \
    -e -o ../results/redis-queue-report

if [ $? -eq 0 ]; then
    echo "Redis queue performance test completed successfully!"
    echo "Results saved to: ../results/redis-queue-results.jtl"
    echo "HTML report generated at: ../results/redis-queue-report"
else
    echo "Redis queue performance test failed!"
    exit 1
fi

echo "Test completed at: $(date)"
