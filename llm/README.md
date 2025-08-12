# LLM Runner (GGUF via llama.cpp)

This is a minimal C++ runner that loads a GGUF LLM and generates text using the `llama.cpp` C API. It supports CPU execution and optional GPU acceleration via GGML backends such as Vulkan or OpenCL (no CUDA/TensorRT/TensorFlow/PyTorch used).

## Build

Prerequisites on Ubuntu:

- CMake >= 3.21
- A C++ compiler (g++/clang++)
- Git
- Optional: Vulkan SDK or OpenCL ICD/headers if enabling those backends

Configure and build:

```bash
cd llm
cmake -B build -S . -DGGML_VULKAN=OFF -DGGML_OPENCL=OFF -DCMAKE_BUILD_TYPE=Release
cmake --build build -j
```

To enable OpenCL (if drivers are installed):

```bash
cmake -B build -S . -DGGML_OPENCL=ON -DCMAKE_BUILD_TYPE=Release
cmake --build build -j
```

To enable Vulkan (if drivers are installed):

```bash
cmake -B build -S . -DGGML_VULKAN=ON -DCMAKE_BUILD_TYPE=Release
cmake --build build -j
```

Note: You should not enable multiple heavy backends simultaneously unless you know what you are doing.

## Run

You need a GGUF model, e.g. from the llama.cpp ecosystem.

```bash
./build/llm_runner -m /path/to/model.gguf -p "Hello, my name is" -n 64 -t 8 -ngl 20 -b 512 -ub
```

- `-m`: model path (.gguf)
- `-p`: prompt text
- `-n`: max tokens to generate
- `-t`: CPU threads
- `-ngl`: number of layers to offload to GPU when a GPU backend is enabled
- `-b`: decode batch size
- `-ub`: use mmap when loading model (lower peak RAM)

## Notes

- This project fetches and builds `llama.cpp` automatically via CMake. The build will respect `GGML_VULKAN` / `GGML_OPENCL` options passed to the top-level CMake.
- The sampling loop uses greedy decoding for simplicity; you can expand it to temperature/top-p/etc. using the logits buffer and `llama_sampler_*` helpers from `llama.cpp` examples.
- No other ML frameworks or hardware-specific SDKs (CUDA/TensorRT/TensorFlow/PyTorch) are required.