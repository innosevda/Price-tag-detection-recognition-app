import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:io';
import 'dart:typed_data';
import 'package:image/image.dart' as img;
import 'package:googleapis/drive/v3.dart' as drive;
import 'package:googleapis_auth/auth_io.dart';
import 'package:http/http.dart' as http;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Permission.storage.request();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Price Tag Reader',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const ImageProcessingApp(),
    );
  }
}

class ImageProcessingApp extends StatefulWidget {
  const ImageProcessingApp({super.key});

  @override
  State<ImageProcessingApp> createState() => _ImageProcessingAppState();
}

class _ImageProcessingAppState extends State<ImageProcessingApp> {
  final ImagePicker _picker = ImagePicker();
  File? _image;
  Uint8List? _processedImage;

  Future<void> _showChoiceDialog(BuildContext context) async {
    return showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Choose an option'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.photo_library),
                title: const Text('Gallery'),
                onTap: () {
                  _getImageFromGallery();
                  Navigator.pop(context);
                },
              ),
              ListTile(
                leading: const Icon(Icons.camera_alt),
                title: const Text('Camera'),
                onTap: () {
                  _getImageFromCamera();
                  Navigator.pop(context);
                },
              ),
            ],
          ),
        );
      },
    );
  }

  Future<void> _getImageFromGallery() async {
    final status = await Permission.storage.status;
    if (status.isGranted) {
      final XFile? pickedFile =
          await _picker.pickImage(source: ImageSource.gallery);
      if (pickedFile != null) {
        setState(() {
          _image = File(pickedFile.path);
          _processedImage = null; // Reset processed image
        });
      }
    } else {
      _showPermissionDeniedMessage(Permission.storage);
    }
  }

  Future<void> _getImageFromCamera() async {
    final status = await Permission.camera.status;
    if (status.isGranted) {
      final XFile? pickedFile =
          await _picker.pickImage(source: ImageSource.camera);
      if (pickedFile != null) {
        setState(() {
          _image = File(pickedFile.path);
          _processedImage = null; // Reset processed image
        });
      }
    } else {
      _showPermissionDeniedMessage(Permission.camera);
    }
  }

  void _showPermissionDeniedMessage(Permission permission) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: const Text('Permission denied. Please enable permissions in settings.'),
        action: SnackBarAction(
          label: 'Settings',
          onPressed: () {
            openAppSettings();
          },
        ),
      ),
    );
  }

  Future<void> _processImage() async {
    if (_image == null) return;

    Uint8List imageBytes = await _image!.readAsBytes();

    // Clip and resize the image
    Uint8List processedImage = clipAndZoomImage(imageBytes);

    setState(() {
      _processedImage = processedImage;
    });

    // Upload to Google Drive
    await uploadToDrive(processedImage);
  }

  Uint8List clipAndZoomImage(Uint8List imageBytes) {
    // Decode the image
    img.Image original = img.decodeImage(imageBytes)!;

    // Calculate center region
    int centerX = original.width ~/ 2;
    int centerY = original.height ~/ 2;

    int cropWidth = (original.width ~/ 3); // Adjust based on zoom level
    int cropHeight = (original.height ~/ 3);

    // Crop to center
    img.Image cropped = img.copyCrop(
      original,
      centerX - cropWidth ~/ 2,
      centerY - cropHeight ~/ 2,
      cropWidth,
      cropHeight,
    );

    // Resize (zoom) the image
    img.Image resized = img.copyResize(cropped, width: cropped.width * 3 ~/ 2);

    // Encode back to Uint8List
    return Uint8List.fromList(img.encodePng(resized));
  }

  Future<void> uploadToDrive(Uint8List imageBytes) async {
  const _scopes = [drive.DriveApi.driveFileScope];

  // Use the iOS client ID
  final clientId = ClientId('318016784027-np2pjjtulnirrmgin2s27s5ee80a9h1g.apps.googleusercontent.com', null); // No client secret for iOS apps

  final authClient = await clientViaUserConsent(clientId, _scopes, (url) {
    print('Please navigate to the following URL to grant access:');
    print(url);
  });

  final driveApi = drive.DriveApi(authClient);

  try {
    // Prepare the file for upload
    var media = drive.Media(Stream.fromIterable([imageBytes]), imageBytes.length);
    var driveFile = drive.File()..name = 'modified_image.png';

    await driveApi.files.create(driveFile, uploadMedia: media);
    print('Image successfully uploaded to Google Drive.');
  } finally {
    authClient.close();
  }
}

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Image Processing'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (_image != null)
              Image.file(
                _image!,
                width: 200,
                height: 200,
                fit: BoxFit.cover,
              ),
            if (_processedImage != null)
              Image.memory(
                _processedImage!,
                width: 200,
                height: 200,
                fit: BoxFit.cover,
              ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                _showChoiceDialog(context);
              },
              child: const Text('Select Image'),
            ),
            ElevatedButton(
              onPressed: _processImage,
              child: const Text('Process & Upload'),
            ),
          ],
        ),
      ),
    );
  }
}
