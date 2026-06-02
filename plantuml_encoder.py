#!/usr/bin/env python3
"""
PlantUML encoder — genereaza URL pentru PlantUML server.
Echivalent Python al encoderului TypeScript.

Utilizare din cod:
    from plantuml_encoder import generate_plantuml_url
    url = generate_plantuml_url("@startuml\nA --> B\n@enduml")
    print(url)  # https://www.plantuml.com/plantuml/png/...

Utilizare din terminal:
    python plantuml_encoder.py diagram.puml
    python plantuml_encoder.py < diagram.puml
"""

import sys
import zlib

PLANTUML_SERVER = "https://www.plantuml.com/plantuml/png/"

# Acelasi alfabet custom ca in versiunea TypeScript
_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"


def _encode64(data: bytes) -> str:
    """Encodeaza bytes cu alfabetul custom PlantUML (nu standard base64)."""
    result = []
    for i in range(0, len(data), 3):
        b1 = data[i]
        b2 = data[i + 1] if i + 1 < len(data) else 0
        b3 = data[i + 2] if i + 2 < len(data) else 0

        c1 =  b1 >> 2
        c2 = ((b1 & 0x3) << 4) | (b2 >> 4)
        c3 = ((b2 & 0xF) << 2) | (b3 >> 6)
        c4 =   b3 & 0x3F

        result.append(_CHARS[c1] + _CHARS[c2] + _CHARS[c3] + _CHARS[c4])
    return "".join(result)


def _compress(text: str) -> bytes:
    """
    Compresie DEFLATE raw (fara header zlib) — formatul asteptat de PlantUML server.
    Versiunea TypeScript nu comprima deloc; aceasta varianta Python face compresie
    reala, ceea ce produce URL-uri mai scurte si compatibile cu serverul oficial.
    """
    data = text.encode("utf-8")
    obj = zlib.compressobj(zlib.Z_BEST_COMPRESSION, zlib.DEFLATED, -15)
    compressed = obj.compress(data) + obj.flush()
    return compressed


def generate_plantuml_url(uml: str) -> str:
    """Genereaza URL PNG pentru PlantUML server din codul UML."""
    encoded = _encode64(_compress(uml))
    return PLANTUML_SERVER + encoded


def generate_plantuml_svg_url(uml: str) -> str:
    """Genereaza URL SVG (vector, mai bun pentru documentatie)."""
    encoded = _encode64(_compress(uml))
    return "https://www.plantuml.com/plantuml/svg/" + encoded


# ---------------------------------------------------------------------------
# CLI — folosit direct din terminal sau integrat cu static_analyzer
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    if len(sys.argv) > 1:
        # python plantuml_encoder.py diagram.puml
        with open(sys.argv[1], encoding="utf-8") as f:
            uml = f.read()
    else:
        # cat diagram.puml | python plantuml_encoder.py
        uml = sys.stdin.read()

    if not uml.strip():
        print("Eroare: input gol.", file=sys.stderr)
        sys.exit(1)

    print("PNG:", generate_plantuml_url(uml))
    print("SVG:", generate_plantuml_svg_url(uml))
