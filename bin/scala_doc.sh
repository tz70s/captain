#!/bin/bash

cd ..
rm -rf docs
sbt doc
mkdir -p docs
cp -r ./captain-framework/target/scala-2.12/api/* docs