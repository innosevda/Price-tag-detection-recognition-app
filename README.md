# A Mobile Application for Visually Impaired People to Read Price Tags

## Introduction
This mobile application is designed to assist visually impaired individuals in reading price tags while shopping. By using the phone's camera, the app scans price tags and provides voice feedback, enabling users to make informed purchasing decisions independently.

## Branches
### `mobile`
This branch contains all files related to the mobile application, including UI components, functionality, and the integration of the backend services. Start here to explore and run the mobile app.

### `fastapi`
This branch handles the server-side logic, including API endpoints used to process and return scanned text data to the mobile app. Visit this branch to run and configure the backend server.

### `model/pipeline`
Previously used for the computer vision model, this branch is now deprecated and has been replaced by Meta Llama for improved text recognition and processing.

## Features
- **Price Tag Detection**: Uses the phone's camera to detect and scan price tags.
- **Text Recognition (OCR)**: Extracts text from price tags and converts it into readable data.
- **Voice Feedback**: Reads out the scanned price tag information for the user.
- **User-Friendly Interface**: Simple and intuitive UI designed for visually impaired users.

## Team Members
- **Tural Gasimov** – Developer  
  Responsible for full mobile and backend development, as well as the integration of all components.

- **Fidan Yusifova** and **Sevda Aliyeva** – Data Scientists  
  Responsible for ensuring efficient text recognition and processing.

- **Ibrahim Aliyev** – Research Writer  
  Supports the project through documentation and research contributions.

## Technologies Used
- **Java/Kotlin** for Android mobile application development.
- **Python/FastAPI** for backend server development.
- **Meta Llama** for text recognition and processing.
- **Amazon EWS** for server hosting and deployment.
- **Android OS** as the target platform for the mobile application.

## Usage
Switch to the respective branches for more information and setup instructions:
- Start with the `mobile` branch to access the mobile application.
- Then move to the `fastapi` branch to configure and run the backend server.
