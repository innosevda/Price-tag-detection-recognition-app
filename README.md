# FastAPI Backend for Mobile App

## Overview
This FastAPI module serves as the backend for a mobile application, integrating with a Computer Vision module to handle requests and responses efficiently. It is designed for high performance and scalability, deployed on AWS EC2 for production use.

## Features
- FastAPI framework for high-performance asynchronous web services
- Integration with a Computer Vision module
- Deployed on AWS EC2 for scalability
- Local development setup for testing and improvements

## Deployment
The backend is deployed on **AWS EC2**, ensuring availability and scalability for production use.

## Local Development Setup
To run the backend locally, follow these steps:

### 1. Clone the Repository
```sh
git clone <repository-url>
cd <repository-name>
```

### 2. Install Dependencies
Ensure you have Python installed. Then, install the required dependencies:
```sh
pip install -r requirements.txt
```

### 3. Run the Application
Start the FastAPI server using Uvicorn:
```sh
python -m uvicorn main:app --reload
```
The `--reload` flag enables auto-reloading for development.

---

For any questions or issues, please contact tgasimov16051@ada.edu.az.

