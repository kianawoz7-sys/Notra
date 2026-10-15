"""Developer-only synthetic Indonesian fixtures; never included in the user APK.

Requires edge-tts and imageio-ffmpeg in the developer's D: Python target.
Uses internet only on the development PC to synthesize fixtures. Android tests
then decode their local PCM in airplane mode; this is not real-microphone QA.
"""
import asyncio
import os
from pathlib import Path
import subprocess

import edge_tts
import imageio_ffmpeg
import numpy as np

ROOT = Path(r"D:\CatatCatat-tools\test-audio")
CASES = {
    "expenses": ("Nasi ayam lima belas ribu, es teh lima ribu.", "+0%"),
    "single": ("Macha dua belas ribu.", "+0%"),
    "reported-six": ("Makan lima belas ribu.", "+0%"),
    "colloquial": ("Macha dua belas rebu, es teh lima rebu.", "+0%"),
    "four": ("Kost delapan ratus ribu, paket data lima puluh ribu, ngeprint lima ribu, bensin tiga puluh ribu.", "+0%"),
    "cuci-normal": ("Cuci dua belas ribu.", "+0%"),
    "cuci-fast": ("Cuci dua belas ribu.", "+20%"),
    "parkir-normal": ("Parkir dua ribu.", "+0%"),
    "chatgpt-normal": ("Langganan ChatGPT seratus enam puluh lima ribu.", "+0%"),
    "devices-normal": ("Charger tiga puluh ribu, speaker seratus dua puluh ribu.", "+0%"),
}


async def main():
    ROOT.mkdir(parents=True, exist_ok=True)
    ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
    for name, (text, rate) in CASES.items():
        mp3 = ROOT / f"{name}.mp3"
        if not mp3.exists():
            await edge_tts.Communicate(text, "id-ID-ArdiNeural", rate=rate).save(str(mp3))
        raw = subprocess.check_output(
            [ffmpeg, "-v", "error", "-i", str(mp3), "-f", "s16le", "-ac", "1", "-ar", "16000", "-"],
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
        audio = np.frombuffer(raw, dtype="<i2")
        audio = np.pad(audio, (8000, 8000))
        if name == "reported-six":
            if len(audio) > 96000:
                raise ValueError("Reported-six fixture unexpectedly exceeds six seconds")
            audio = np.pad(audio, (0, 96000 - len(audio)))
        (ROOT / f"{name}.pcm").write_bytes(audio.astype("<i2").tobytes())
        if name == "cuci-normal":
            (ROOT / "cuci-quiet.pcm").write_bytes((audio * .03).astype("<i2").tobytes())
        print(name, round(len(audio) / 16000, 2), "seconds", flush=True)


if __name__ == "__main__":
    os.environ.setdefault("PYTHONDONTWRITEBYTECODE", "1")
    asyncio.run(main())
