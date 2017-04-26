#!/usr/bin/env bash

### Deletes compiled artist files for a given sdk
### Expects the sdk to remove artist files for as the first argument

sdk_level=$1

cd app/src/main/assets/artist

echo "Deleting compiled artist files for sdk level ${sdk_level}"

rm -rf android-${sdk_level}/lib/
rm -rf android-${sdk_level}/dex2oat