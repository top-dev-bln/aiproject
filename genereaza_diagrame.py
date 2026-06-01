import os
import sys

# Importa generatorul Python
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from java2puml import generate_puml

# --- CONFIGURARE ---
DIRECTOR_SURSA = "dataset_brut"
DIRECTOR_DESTINATIE = "diagrame_puml"
# -------------------

def main():
    if not os.path.exists(DIRECTOR_DESTINATIE):
        os.makedirs(DIRECTOR_DESTINATIE)

    proiecte = [
        nume for nume in os.listdir(DIRECTOR_SURSA)
        if os.path.isdir(os.path.join(DIRECTOR_SURSA, nume))
    ]
    total = len(proiecte)

    print(f"Am găsit {total} proiecte. Încep generarea diagramelor...\n")

    succes = 0
    esuat = 0

    for index, nume_proiect in enumerate(proiecte, 1):
        cale_proiect = os.path.join(DIRECTOR_SURSA, nume_proiect)
        cale_output = os.path.join(DIRECTOR_DESTINATIE, f"{nume_proiect}.puml")

        if os.path.exists(cale_output):
            print(f"[{index}/{total}] Sar peste '{nume_proiect}' - diagrama există deja.")
            continue

        print(f"[{index}/{total}] Procesez: {nume_proiect} ...", end=" ", flush=True)

        try:
            generate_puml(cale_proiect, cale_output)
            succes += 1
        except Exception as e:
            print(f"\n    -> [Eșuat] {e}")
            esuat += 1

    print(f"\n[✔] Gata: {succes} succes, {esuat} eșuate. Diagrame în: {DIRECTOR_DESTINATIE}/")

if __name__ == "__main__":
    main()
