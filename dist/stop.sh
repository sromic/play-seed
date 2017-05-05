#!/bin/bash
pid=`cat RUNNING_PID`
echo "Stopping application"
kill -SIGTERM $pid