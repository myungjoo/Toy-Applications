#include <cstdio>
#include <cstring>
#include <string>
#include <vector>
#include <iostream>
#include <thread>
#include <algorithm>

#include "llama.h"

static void print_usage(const char * prog) {
    std::fprintf(stderr,
        "Usage: %s -m <model.gguf> [-p prompt] [-n n_tokens] [-t threads] [-ngl n_gpu_layers] [-b batch] [-ub]\\n"
        "Options:\n"
        "  -m <path>      Path to GGUF model file\n"
        "  -p <prompt>    Prompt text (default: empty)\n"
        "  -n <int>       Max tokens to generate (default: 128)\n"
        "  -t <int>       Number of CPU threads (default: max)\n"
        "  -ngl <int>     Number of GPU layers to offload (if backend supports) (default: 0)\n"
        "  -b <int>       Batch size for decode (default: 512)\n"
        "  -ub            Use memory-mapped files (avoid huge RAM spikes)\n",
        prog);
}

int main(int argc, char ** argv) {
    std::string model_path;
    std::string prompt;
    int32_t n_predict = 128;
    int32_t n_threads = std::max(1u, std::thread::hardware_concurrency());
    int32_t n_gpu_layers = 0;
    int32_t n_batch = 512;
    bool use_mmap = false;

    for (int i = 1; i < argc; ++i) {
        std::string arg = argv[i];
        if (arg == "-m" && i+1 < argc) { model_path = argv[++i]; }
        else if (arg == "-p" && i+1 < argc) { prompt = argv[++i]; }
        else if (arg == "-n" && i+1 < argc) { n_predict = std::stoi(argv[++i]); }
        else if (arg == "-t" && i+1 < argc) { n_threads = std::stoi(argv[++i]); }
        else if (arg == "-ngl" && i+1 < argc) { n_gpu_layers = std::stoi(argv[++i]); }
        else if (arg == "-b" && i+1 < argc) { n_batch = std::stoi(argv[++i]); }
        else if (arg == "-ub") { use_mmap = true; }
        else { print_usage(argv[0]); return 1; }
    }

    if (model_path.empty()) {
        print_usage(argv[0]);
        return 1;
    }

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = n_gpu_layers;
    model_params.use_mmap = use_mmap;
    model_params.use_mlock = false;

    llama_model * model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (!model) {
        std::fprintf(stderr, "Failed to load model: %s\n", model_path.c_str());
        return 1;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_threads = n_threads;
    ctx_params.n_threads_batch = n_threads;
    ctx_params.n_batch = n_batch;

    llama_context * ctx = llama_init_from_model(model, ctx_params);
    if (!ctx) {
        std::fprintf(stderr, "Failed to create context\n");
        llama_model_free(model);
        return 1;
    }

    const llama_vocab * vocab = llama_model_get_vocab(model);

    // Tokenize prompt
    std::vector<llama_token> tokens;
    int n_tokens = 0;
    if (!prompt.empty()) {
        // First call with a reasonably large buffer; if negative, re-alloc to required size
        tokens.resize(prompt.size() + 64);
        n_tokens = llama_tokenize(
            vocab,
            prompt.c_str(),
            (int32_t)prompt.size(),
            tokens.data(),
            (int32_t)tokens.size(),
            /*add_special=*/true,
            /*parse_special=*/true);
        if (n_tokens < 0) {
            tokens.resize((size_t)(-n_tokens));
            n_tokens = llama_tokenize(
                vocab,
                prompt.c_str(),
                (int32_t)prompt.size(),
                tokens.data(),
                (int32_t)tokens.size(),
                /*add_special=*/true,
                /*parse_special=*/true);
        }
        tokens.resize(n_tokens);
    }

    llama_batch batch = llama_batch_init(n_batch, 0, /*n_seq_max=*/1);

    // evaluate the prompt if any
    int n_past = 0;
    if (n_tokens > 0) {
        for (int i = 0; i < n_tokens; i += n_batch) {
            batch.n_tokens = std::min(n_batch, n_tokens - i);
            for (int j = 0; j < batch.n_tokens; ++j) {
                batch.token[j] = tokens[i + j];
                batch.pos[j]   = n_past + j;
                batch.n_seq_id[j] = 1;
                batch.seq_id[j][0] = 0;
                // request logits only for the last token of the final chunk
                bool is_last_chunk = (i + batch.n_tokens) >= n_tokens;
                bool is_last_token_in_chunk = (j == batch.n_tokens - 1);
                batch.logits[j] = (is_last_chunk && is_last_token_in_chunk) ? 1 : 0;
            }
            if (llama_decode(ctx, batch)) {
                std::fprintf(stderr, "llama_decode failed on prompt\n");
                llama_batch_free(batch);
                llama_free(ctx);
                llama_model_free(model);
                llama_backend_free();
                return 1;
            }
            n_past += batch.n_tokens;
        }
    }

    // Greedy sampling loop
    std::string generated;
    for (int i = 0; i < n_predict; ++i) {
        const float * logits = llama_get_logits_ith(ctx, -1);
        if (!logits) logits = llama_get_logits(ctx);

        const int vocab_size = llama_vocab_n_tokens(vocab);
        int best_id = 0; float best_logit = logits[0];
        for (int tid = 1; tid < vocab_size; ++tid) {
            if (logits[tid] > best_logit) { best_logit = logits[tid]; best_id = tid; }
        }

        // decode token piece into bytes
        char piece_buf[256];
        int piece_len = llama_token_to_piece(vocab, best_id, piece_buf, (int)sizeof(piece_buf), /*lstrip=*/0, /*special=*/true);
        if (piece_len > 0) {
            std::cout.write(piece_buf, piece_len);
            std::cout.flush();
            generated.append(piece_buf, piece_len);
        }

        // feed the sampled token back
        batch.n_tokens = 1;
        batch.token[0] = (llama_token)best_id;
        batch.pos[0]   = n_past;
        batch.n_seq_id[0] = 1;
        batch.seq_id[0][0] = 0;
        batch.logits[0] = 1; // request logits for next step

        if (llama_decode(ctx, batch)) {
            std::fprintf(stderr, "llama_decode failed during generation\n");
            break;
        }
        n_past += 1;
    }

    std::cout << std::endl;

    llama_batch_free(batch);
    llama_free(ctx);
    llama_model_free(model);
    llama_backend_free();

    return 0;
}