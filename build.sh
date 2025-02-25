#!/usr/bin/env bash

# Install espeak-ng
sudo apt-get update && apt-get install -y espeak-ng

# Install Python dependencies
pip install -r requirements.txt
