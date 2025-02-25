import os
import shutil
import subprocess

from fastapi import FastAPI, UploadFile, File
from starlette.responses import StreamingResponse

app = FastAPI()

class SDPAPP:
    def __init__(self):
        self.image_folder = "images"
        os.makedirs(self.image_folder, exist_ok=True)

    def process_image(self, file: UploadFile):
        # Save image in images folder
        image_path = os.path.join(self.image_folder, file.filename)
        with open(image_path, "wb") as f:
            shutil.copyfileobj(file.file, f)
        return {"message": "Image saved successfully", "filename": file.filename}

    def get_price(self):
        # Example price
        return "10 manat 28 qəpik"

    def generate_audio(self, text: str):
        # Use subprocess to stream audio output
        command = [
            "espeak-ng",
            "-v", "az", # Azerbaijani language
            "-s", "180", # speed = 180
            "-p", "50", # pitch = 50
            "-a", "200", # amplitude/volume = 200
            "--stdout",  # Output to stdout instead of a file
            text  # Text to convert to speech
        ]

        # Run the command and capture the output
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return process.stdout  # Stream the output directly

sdp_app = SDPAPP()

@app.get("/")
async def root():
    return {"state": "running!"}

@app.post("/image/")
async def process_image(file: UploadFile = File(...)):
    return sdp_app.process_image(file)

@app.get("/price/")
def get_price():
    return {"price": sdp_app.get_price()}

@app.get("/audio/")
def generate_audio():
    price_text = sdp_app.get_price()
    audio_stream = sdp_app.generate_audio(price_text)

    # Stream the audio back to the mobile app
    return StreamingResponse(audio_stream, media_type="audio/wav")