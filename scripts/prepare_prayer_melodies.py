#!/usr/bin/env python3
import html
import os
import re
import subprocess
import tempfile
import urllib.request
from pathlib import Path

TRACKS = [
    (
        "491386",
        "https://pixabay.com/music/world-oasis-of-peace-islamic-ambient-background-music-for-spiritual-calm-491386/",
        "melody_oasis.mp3",
    ),
    (
        "503897",
        "https://pixabay.com/music/world-arabic-nasheed-%D8%AD%D8%A8%D9%8A%D8%A8-%D9%82%D9%84%D8%A8%D9%8A-503897/",
        "melody_nasheed.mp3",
    ),
    (
        "586137",
        "https://pixabay.com/music/arabic-arabic-sufi-ney-background-586137/",
        "melody_ney.mp3",
    ),
]

UA = (
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/130.0 Safari/537.36"
)

def fetch(url: str, referer: str | None = None) -> bytes:
    headers = {"User-Agent": UA, "Accept": "*/*"}
    if referer:
        headers["Referer"] = referer
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=45) as response:
        return response.read()

def audio_url_from_page(page_url: str, track_id: str) -> str:
    page = fetch(page_url).decode("utf-8", errors="replace")
    page = html.unescape(page).replace("\\u002F", "/").replace("\\/", "/")

    candidates = []
    patterns = [
        r'https://cdn\.pixabay\.com/download/audio/[^"\'<>\s]+',
        r'https://cdn\.pixabay\.com/audio/[^"\'<>\s]+',
    ]
    for pattern in patterns:
        candidates.extend(re.findall(pattern, page))

    cleaned = []
    for url in candidates:
        url = url.rstrip("\\")
        if ".mp3" not in url.lower():
            continue
        if url not in cleaned:
            cleaned.append(url)

    selected = next((u for u in cleaned if track_id in u), None)
    if not selected:
        print(f"Could not identify audio URL for track {track_id}.")
        print("MP3 candidates found:")
        for item in cleaned[:20]:
            print(item)
        raise RuntimeError(f"Pixabay audio URL not found for {track_id}")
    return selected

def main() -> None:
    output_dir = Path("app/src/main/res/raw")
    output_dir.mkdir(parents=True, exist_ok=True)

    for track_id, page_url, output_name in TRACKS:
        print(f"Preparing {track_id}: {output_name}")
        audio_url = audio_url_from_page(page_url, track_id)
        print(f"Source: {audio_url.split('?')[0]}")
        source_bytes = fetch(audio_url, referer=page_url)

        with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as temp:
            temp.write(source_bytes)
            source_path = temp.name

        output_path = output_dir / output_name
        try:
            subprocess.check_call(
                [
                    "ffmpeg",
                    "-hide_banner",
                    "-loglevel",
                    "error",
                    "-y",
                    "-i",
                    source_path,
                    "-t",
                    "15",
                    "-vn",
                    "-ac",
                    "1",
                    "-ar",
                    "22050",
                    "-b:a",
                    "96k",
                    str(output_path),
                ]
            )
        finally:
            os.unlink(source_path)

        if output_path.stat().st_size < 10_000:
            raise RuntimeError(f"Generated audio is unexpectedly small: {output_path}")
        print(f"OK: {output_path} ({output_path.stat().st_size} bytes)")

if __name__ == "__main__":
    main()
