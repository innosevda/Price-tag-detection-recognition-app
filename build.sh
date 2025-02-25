#!/usr/bin/env bash

# Install espeak-ng
apt-get update && apt-get install -y espeak-ng

# Install Python dependencies
pip install -r requirements.txt