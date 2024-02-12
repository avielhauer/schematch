#!/bin/bash

# Default values
classpath="metanome-cli-1.1.0.jar:pyro-distro-1.0-SNAPSHOT-distro.jar"
main_class="de.metanome.cli.App"
algorithm="de.hpi.isg.pyro.algorithms.Pyro"
input_key="inputFile"
input_file="real_iris.csv"
separator=","
max_ucc_error="0.1"
output="print"

# Assemble the command
command="java -cp $classpath $main_class --algorithm $algorithm --input-key $input_key --files $input_file --output $output --separator $separator --header --algorithm-config maxUccError:$max_ucc_error"

# Execute the command
echo "Executing: $command"
eval $command
