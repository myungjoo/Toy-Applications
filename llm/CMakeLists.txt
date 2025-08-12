cmake_minimum_required(VERSION 3.21)
project(llm_runner LANGUAGES C CXX)

# Options to control ggml/llama backends at configure time
# You can override these when invoking CMake, e.g.:
#   -DGGML_VULKAN=ON -DGGML_OPENCL=OFF -DGGML_CUDA=OFF -DGGML_BLAS=OFF
option(GGML_VULKAN "Enable GGML Vulkan backend" OFF)
option(GGML_OPENCL "Enable GGML OpenCL backend" OFF)
option(GGML_CUDA   "Enable GGML CUDA backend (not used here)" OFF)
option(GGML_METAL  "Enable GGML Metal backend (not used on Linux)" OFF)
option(GGML_BLAS   "Enable GGML BLAS (OpenBLAS/Accelerate/etc.)" OFF)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

include(FetchContent)

# Fetch llama.cpp
# Note: You can pin a specific commit for reproducibility by replacing GIT_TAG with a commit hash
FetchContent_Declare(
  llama_cpp
  GIT_REPOSITORY https://github.com/ggerganov/llama.cpp.git
  GIT_TAG        master
  GIT_SHALLOW    TRUE
)

# Propagate config flags down to llama.cpp / ggml
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(GGML_VULKAN ${GGML_VULKAN} CACHE BOOL "" FORCE)
set(GGML_OPENCL ${GGML_OPENCL} CACHE BOOL "" FORCE)
set(GGML_CUDA   ${GGML_CUDA}   CACHE BOOL "" FORCE)
set(GGML_METAL  ${GGML_METAL}  CACHE BOOL "" FORCE)
set(GGML_BLAS   ${GGML_BLAS}   CACHE BOOL "" FORCE)

FetchContent_MakeAvailable(llama_cpp)

add_executable(llm_runner
  src/main.cpp
)

# Link against llama target exported by llama.cpp
# This brings in ggml and selected backends automatically
if(TARGET llama)
  target_link_libraries(llm_runner PRIVATE llama)
else()
  # Fallback for older/newer target names
  target_link_libraries(llm_runner PRIVATE llama-static)
endif()

# Prefer higher optimization for release
if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Release)
endif()

if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
  target_compile_options(llm_runner PRIVATE -O3 -fno-omit-frame-pointer -march=native)
endif()