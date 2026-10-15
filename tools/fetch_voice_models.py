"""Download pinned public model weights to D; nothing is fetched by the Android app."""
import argparse
import concurrent.futures
import hashlib
import pathlib
import time
import urllib.request

REVISION = "5359861c739e955e79d9a303bcbc70fb988958b1"
MODELS = {
    "small": ("ggml-small-q5_1.bin", 190085487, "ae85e4a935d7a567bd102fe55afc16bb595bdb618e11b2fc7591bc08120411bb"),
    "base": ("ggml-base-q5_1.bin", 59707625, "422f1ae452ade6f30a004d7e5c6a43195e4433bc370bf23fac9cc591f01a8898"),
}
ROOT = pathlib.Path("D:/CatatCatat-tools")
CHUNK = 2 * 1024 * 1024


def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        while block := stream.read(1024 * 1024):
            digest.update(block)
    return digest.hexdigest()


def download(model):
    name, size, expected = MODELS[model]
    target = ROOT / "models" / name
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists() and target.stat().st_size == size and sha256(target) == expected:
        print("Verified existing", name, flush=True)
        return
    parts = ROOT / "downloads" / (name + ".parts")
    parts.mkdir(parents=True, exist_ok=True)

    def fetch_part(index):
        start = index * CHUNK
        end = min(size, start + CHUNK) - 1
        part = parts / str(index)
        if part.exists() and part.stat().st_size == end - start + 1:
            return part
        for attempt in range(5):
            url = f"https://huggingface.co/ggerganov/whisper.cpp/resolve/{REVISION}/{name}?download=true&notra={time.time_ns()}"
            request = urllib.request.Request(url, headers={"Range": f"bytes={start}-{end}", "User-Agent": "Notra-build"})
            try:
                with urllib.request.urlopen(request, timeout=60) as response:
                    if response.status != 206 or response.headers.get("Content-Range") != f"bytes {start}-{end}/{size}":
                        raise RuntimeError("Unexpected range response")
                    data = response.read(end - start + 2)
                if len(data) != end - start + 1:
                    raise RuntimeError("Incomplete model range")
                part.write_bytes(data)
                return part
            except Exception:
                if attempt == 4:
                    raise
                time.sleep(attempt + 1)

    count = (size + CHUNK - 1) // CHUNK
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        futures = [pool.submit(fetch_part, i) for i in range(count)]
        for done, future in enumerate(concurrent.futures.as_completed(futures), 1):
            future.result()
            if done % 8 == 0 or done == count:
                print(name, f"{done}/{count} chunks", flush=True)
    partial = target.with_suffix(target.suffix + ".part")
    with partial.open("wb") as output:
        for index in range(count):
            output.write((parts / str(index)).read_bytes())
    if partial.stat().st_size != size or sha256(partial) != expected:
        # Do not reuse same-length corrupt chunks forever on the next attempt.
        for index in range(count):
            (parts / str(index)).unlink(missing_ok=True)
        partial.unlink(missing_ok=True)
        raise RuntimeError("Model checksum does not match the pinned release; rerun to download fresh chunks")
    partial.replace(target)
    for index in range(count):
        (parts / str(index)).unlink()
    parts.rmdir()
    print("Verified", name, size, expected, flush=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", choices=["small", "base", "all"], default="small")
    choice = parser.parse_args().model
    for model in MODELS if choice == "all" else [choice]:
        download(model)
