# SoundWatch

![Status](https://img.shields.io/badge/Version-Experimental-brightgreen.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview

SoundWatch is a cutting-edge Android application for smartwatches that provides real-time sound awareness through visual and vibration feedback. Using advanced deep learning technology, it helps users identify sounds, measure loudness, and track sound occurrences in their environment.

Key Features:
- Real-time sound detection and classification
- Customizable sound alerts
- Visual and vibration feedback
- Multiple deployment architectures (watch-only, watch+phone, watch+phone+cloud, watch+cloud)
- Privacy-focused design

[[Website](https://makeabilitylab.cs.washington.edu/project/soundwatch/)] |
[[Research Paper](https://homes.cs.washington.edu/~djain/img/portfolio/Jain_SoundWatch_ASSETS2020.pdf)]

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Installation Guide](#installation-guide)
3. [Project Structure](#project-structure)
4. [Configuration](#configuration)
5. [Performance Testing](#performance-testing)
6. [Compatible Devices](#compatible-devices)
7. [Troubleshooting](#troubleshooting)
8. [Support](#support)
9. [Acknowledgements](#acknowledgements)

## System Requirements

### Development Environment
- Android Studio 4.0 or later
- Android SDK 28
- Android Build Tools v28.0.3
- Android Support Repository
- Python 3.4 or later
- Git

### Device Requirements
- Android Wear OS device (see [Compatible Devices](#compatible-devices))
- Android phone running Android 6.0 (Marshmallow) or later
- Bluetooth and WiFi connectivity

## Installation Guide

### Step 1: Clone the Repository
```bash
git clone https://github.com/YourUsername/SoundWatch.git
cd SoundWatch
```

### Step 2: Download Required Models
1. Download the sound classification model and labels from [here](https://www.dropbox.com/sh/wngu1kuufwdk8nr/AAC1rm5QR-amL_HBzTOgsZnca?dl=0)
2. Place the `.tflite` model and `labels.txt` files in:
   - `SoundWatch/Application/src/main/assets/`
   - `SoundWatch/Wearable/src/main/assets/`

### Step 3: Configure Python Environment
1. Install Python 3.4 or later
2. Update the `build.gradle` files in both Application and Wearable modules:
```gradle
python {
    buildPython "PATH_TO_YOUR_PYTHON_EXECUTABLE"
    pip {
        install "numpy==1.14.2"
    }
}
```

### Step 4: Build Configuration
1. Open the project in Android Studio
2. Sync project with Gradle files
3. Configure the architecture in `MainActivity.java`:
```java
// Choose one:
WATCH_ONLY_ARCHITECTURE
WATCH_PHONE_ARCHITECTURE
WATCH_PHONE_CLOUD_ARCHITECTURE
WATCH_CLOUD_ARCHITECTURE
```

### Step 5: Deploy
1. Connect your Android Wear device
2. Select the 'Wearable' configuration
3. Click 'Run' in Android Studio

## Project Structure

```
SoundWatch/
├── server/           # Python server for cloud processing
├── Application/      # Android phone application
└── Wearable/        # Android watch application
```

## Configuration

### Architecture Options
- **Watch-Only**: Processing on the watch
- **Watch+Phone**: Distributed processing
- **Watch+Phone+Cloud**: Hybrid processing
- **Watch+Cloud**: Cloud-based processing

### Audio Transmission Styles
- **Raw Audio**: Faster processing, higher bandwidth
- **Audio Features**: Enhanced privacy, lower bandwidth

## Performance Testing

Enable testing flags in `MainActivity.java`:
```java
// For model latency testing
TEST_MODEL_LATENCY = true

// For end-to-end latency testing
TEST_E2E_LATENCY = true
```

Results are saved to:
- `watch_model.txt`
- `e2e_latency.txt`

## Compatible Devices

### Budget-Friendly Options (Under $200)
- Fossil Sport
- Ticwatch E2
- Ticwatch S2
- Skagen Falster 3

### Mid-Range Options ($200-$300)
- Ticwatch Pro 2/3 (Recommended)
- Fossil Gen 5
- Moto 360
- Oppo Watch

### Premium Options ($300+)
- Suunto 7

## Troubleshooting

### Common Issues and Solutions

1. **Device Pairing Issues**
   - Reopen WearOS app on phone
   - Wait for "Connected via Bluetooth|Wifi" status
   - Restart SoundWatch on watch

2. **Sound Detection Issues**
   - Ensure microphone permissions are granted
   - Check battery level
   - Verify selected sounds in phone app

3. **Performance Issues**
   - Clear app cache
   - Check available storage
   - Ensure watch is properly charged

## Support

### Technical Support
- Primary Contact: [Dhruv Jain](https://homes.cs.washington.edu/~djain/)
- Development Team:
  - [Khoa Nguyen](https://www.linkedin.com/in/akka/) (akhoa99@cs.washington.edu)
  - [Hung V Ngo](http://www.hungvngo.com) (hvn297@cs.washington.edu)

### Licensing
Chaquopy SDK requires a license. Contact the development team for details.

## Acknowledgements

Built with support from:
- [MakeabilityLab](https://makeabilitylab.cs.washington.edu/)
- NSF grant [IIS-1763199](https://www.nsf.gov/awardsearch/showAward?AWD_ID=1763199)

Technologies:
- [Data layer sample](https://github.com/android/wear-os-samples/tree/master/DataLayer)
- [Socket.io](https://socket.io/)
- [Chaquopy](https://chaquo.com/chaquopy/)

## Related Projects

- [HomeSound](https://makeabilitylab.cs.washington.edu/project/smarthomedhh/): In-Home Sound Awareness System

---

For research inquiries or technical support, please contact the development team. We welcome collaboration and feedback to improve SoundWatch.



