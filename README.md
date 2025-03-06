# Mobile Application Model for Visually Impaired - Model Branch

## Overview
This branch contains the model-related files for our mobile application, which is designed to assist visually impaired individuals by reading prices from images. The models and preprocessing steps are stored in Google Colab notebooks, along with a folder containing example images for testing purposes.

## Contents
- **Google Colab Notebooks**
  - `Preprocessing.ipynb` - Handles image preprocessing before text extraction.
  - `PaddleOCR.ipynb` - Implements OCR (Optical Character Recognition) using PaddleOCR to extract price information from images.
  - Additional notebooks for testing and model experimentation.

- **test_images/**
  - A folder containing example images for testing the models.

## Usage
1. Open `Preprocessing.ipynb` in Google Colab and run the cells to preprocess input images.
2. Use `PaddleOCR.ipynb` to extract price information from the processed images.
3. The test images in `test_images/` can be used to evaluate the effectiveness of the model.

## Future Improvements
- Extracting other information (name of product and more).
- Improving preprocessing techniques for more robust OCR performance.

