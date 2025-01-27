$sdkRoot = "C:\Users\hirwa\AppData\Local\Android\Sdk"
$cmdlineToolsZip = "commandlinetools-win-10406996_latest.zip"
$cmdlineToolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip"
$tempDir = "$sdkRoot\.temp"

# Create directories if they don't exist
New-Item -ItemType Directory -Force -Path $sdkRoot
New-Item -ItemType Directory -Force -Path $tempDir
New-Item -ItemType Directory -Force -Path "$sdkRoot\cmdline-tools"

# Download command line tools
Write-Host "Downloading Android Command Line Tools..."
Invoke-WebRequest -Uri $cmdlineToolsUrl -OutFile "$tempDir\$cmdlineToolsZip"

# Extract command line tools
Write-Host "Extracting Command Line Tools..."
Expand-Archive -Path "$tempDir\$cmdlineToolsZip" -DestinationPath "$sdkRoot\cmdline-tools" -Force

# Move contents to 'latest' directory
New-Item -ItemType Directory -Force -Path "$sdkRoot\cmdline-tools\latest"
Get-ChildItem -Path "$sdkRoot\cmdline-tools\cmdline-tools\*" | Move-Item -Destination "$sdkRoot\cmdline-tools\latest" -Force

# Accept licenses
Write-Host "Accepting SDK licenses..."
$sdkmanager = "$sdkRoot\cmdline-tools\latest\bin\sdkmanager.bat"
echo "y" | & $sdkmanager --licenses

# Install required SDK components
Write-Host "Installing SDK components..."
echo "y" | & $sdkmanager --install "platform-tools" "platforms;android-33" "build-tools;33.0.1" "ndk;25.2.9519653" "cmake;3.22.1"

Write-Host "Setup complete!"
