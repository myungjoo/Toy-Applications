FROM eclipse-temurin:17-jdk

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV ANDROID_HOME=/opt/android-sdk-linux
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Create android sdk directory
RUN mkdir -p ${ANDROID_HOME}

# Download and install Android SDK
RUN wget -O commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
    && unzip commandlinetools.zip -d ${ANDROID_HOME} \
    && rm commandlinetools.zip \
    && mkdir -p ${ANDROID_HOME}/cmdline-tools/latest \
    && mv ${ANDROID_HOME}/cmdline-tools/bin ${ANDROID_HOME}/cmdline-tools/latest/ \
    && mv ${ANDROID_HOME}/cmdline-tools/lib ${ANDROID_HOME}/cmdline-tools/latest/ \
    && mv ${ANDROID_HOME}/cmdline-tools/source.properties ${ANDROID_HOME}/cmdline-tools/latest/ \
    && mv ${ANDROID_HOME}/cmdline-tools/NOTICE.txt ${ANDROID_HOME}/cmdline-tools/latest/

# Accept licenses and install required packages
RUN yes | sdkmanager --licenses
RUN sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x ./gradlew

# Build the APK
RUN ./gradlew assembleRelease

# Copy APK to output location
RUN mkdir -p /output && cp app/build/outputs/apk/release/*.apk /output/

CMD ["echo", "APK built successfully"]