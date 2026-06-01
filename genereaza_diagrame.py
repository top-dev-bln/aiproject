import os
import subprocess

# --- CONFIGURARE ---
DIRECTOR_SURSA = "dataset_brut"
DIRECTOR_DESTINATIE = "diagrame_puml"
GENERATOR_JAR = "PlantUMLGenerator.jar"
# -------------------

def main():
    if not os.path.exists(DIRECTOR_DESTINATIE):
        os.makedirs(DIRECTOR_DESTINATIE)

    if not os.path.exists(GENERATOR_JAR):
        print(f"[Eroare] Nu găsesc {GENERATOR_JAR} în folderul curent!")
        return

    proiecte = [nume for nume in os.listdir(DIRECTOR_SURSA) if os.path.isdir(os.path.join(DIRECTOR_SURSA, nume))]
    total = len(proiecte)

    print(f"Am găsit {total} proiecte. Încep generarea diagramelor...\n")

    for index, nume_proiect in enumerate(proiecte, 1):
        cale_proiect = os.path.join(DIRECTOR_SURSA, nume_proiect)
        cale_output = os.path.join(DIRECTOR_DESTINATIE, f"{nume_proiect}.puml")

        if os.path.exists(cale_output):
            print(f"[{index}/{total}] Sar peste '{nume_proiect}' - diagrama există deja.")
            continue

        print(f"[{index}/{total}] Procesez: {nume_proiect} ...")

        comanda = [
            "java", 
            "-jar", 
            GENERATOR_JAR, 
            cale_proiect, 
            cale_output
        ]

        try:
            subprocess.run(
                comanda, 
                check=True, 
                stdout=subprocess.DEVNULL,
                stderr=subprocess.PIPE,
                text=True
            )
            print("    -> Succes!")
        except subprocess.CalledProcessError as e:
            print(f"    -> [Eșuat] Eroare la analizarea proiectului {nume_proiect}.")

    print("\n[✔] Procesare completă. Verifică folderul:", DIRECTOR_DESTINATIE)

if __name__ == "__main__":
    main()